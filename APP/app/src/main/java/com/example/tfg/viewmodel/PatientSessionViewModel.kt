/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel encargado de orquestar la ejecución de una sesión de rehabilitación por parte del paciente.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.Sesion
import com.example.tfg.model.SesionUpdate
import com.example.tfg.model.SubirVideo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Estados secuenciales que definen el progreso de una sesión de ejercicios activa.
 */
enum class FaseSesion {
    CARGANDO,
    PREPARACION,
    CUENTA_ATRAS,
    GRABANDO,
    REVIEW_EJERCICIO,
    GUARDANDO,
    RESUMEN_FINAL
}

/**
 * Modelo interno simplificado para presentar la información relevante de un ejercicio durante su ejecución.
 */
data class EjercicioDisplay(
    val idEjercicio: Int,
    val nombre: String,
    val descripcion: String,
    val videoUrl: String?,
    val duracionSegundos: Int
)

/**
 * Gestiona el flujo completo de una sesión de terapia controlando temporizadores, grabación de vídeo y subida de resultados.
 */
class PatientSessionViewModel : ViewModel() {

    var faseActual by mutableStateOf(FaseSesion.CARGANDO)
    var listaEjercicios by mutableStateOf<List<EjercicioDisplay>>(emptyList())
    var indiceEjercicioActual by mutableIntStateOf(0)

    var segundosRestantes by mutableIntStateOf(0)
    var conteoRegresivo by mutableIntStateOf(0)

    private var timerJob: Job? = null
    var errorMessage by mutableStateOf("")

    var rutaVideoGrabado: String? by mutableStateOf(null)
    var sesionActual by mutableStateOf<Sesion?>(null)

    private var currentToken: String = ""

    /**
     * Inicia la carga de los detalles de la sesión y sus ejercicios asociados desde el servidor preparando el entorno para comenzar.
     */
    fun cargarSesion(token: String, idSesion: Int) {
        indiceEjercicioActual = 0
        segundosRestantes = 0
        errorMessage = ""
        rutaVideoGrabado = null
        timerJob?.cancel()
        faseActual = FaseSesion.CARGANDO
        currentToken = token

        viewModelScope.launch {
            try {
                val respSesion = RetrofitClient.instance.getSesion("Bearer $token", idSesion)

                if (respSesion.isSuccessful && respSesion.body() != null) {
                    sesionActual = respSesion.body()
                    val idRutina = sesionActual!!.idRutina

                    if (idRutina != null) {
                        cargarEjerciciosDeRutina(token, idRutina)
                    } else {
                        errorMessage = "Esta sesión no tiene rutina asignada"
                        faseActual = FaseSesion.RESUMEN_FINAL
                    }
                } else {
                    errorMessage = "Error al cargar sesión (${respSesion.code()})"
                    faseActual = FaseSesion.RESUMEN_FINAL
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
                faseActual = FaseSesion.RESUMEN_FINAL
            }
        }
    }

    private suspend fun cargarEjerciciosDeRutina(token: String, idRutina: Int) {
        try {
            val respRutina = RetrofitClient.instance.getRutinaCompleta(idRutina, "Bearer $token")

            if (respRutina.isSuccessful && respRutina.body() != null) {
                val rutina = respRutina.body()!!

                listaEjercicios = rutina.fichas.map { ficha ->
                    val duracionMinutos = ficha.tiempoMinutos ?: 0
                    val duracionEjercicio = ficha.ejercicio.duracion ?: 0

                    val tiempoFinalSegundos = if (duracionMinutos > 0) {
                        duracionMinutos * 60
                    } else if (duracionEjercicio > 0) {
                        duracionEjercicio
                    } else {
                        60
                    }

                    EjercicioDisplay(
                        idEjercicio = ficha.ejercicio.id,
                        nombre = ficha.ejercicio.nombre ?: "Ejercicio sin nombre",
                        descripcion = ficha.ejercicio.descripcion ?: "",
                        videoUrl = ficha.ejercicio.videoUrl,
                        duracionSegundos = tiempoFinalSegundos
                    )
                }

                if (listaEjercicios.isNotEmpty()) {
                    faseActual = FaseSesion.PREPARACION
                } else {
                    errorMessage = "La rutina está vacía."
                    faseActual = FaseSesion.RESUMEN_FINAL
                }
            } else {
                errorMessage = "Error cargando rutina (${respRutina.code()})"
                faseActual = FaseSesion.RESUMEN_FINAL
            }
        } catch (e: Exception) {
            errorMessage = "Error procesando rutina: ${e.message}"
            faseActual = FaseSesion.RESUMEN_FINAL
        }
    }

    /**
     * Devuelve la información del ejercicio que se está realizando en este momento.
     */
    fun getEjercicioActual(): EjercicioDisplay? {
        if (listaEjercicios.isEmpty() || indiceEjercicioActual >= listaEjercicios.size) return null
        return listaEjercicios[indiceEjercicioActual]
    }

    /**
     * Activa el temporizador de cuenta regresiva previo al inicio de la grabación del ejercicio.
     */
    fun iniciarSecuenciaGrabacion() {
        rutaVideoGrabado = null
        val tiempoPreparacion = sesionActual?.tiempoPreparacion ?: 5
        conteoRegresivo = if (tiempoPreparacion > 0) tiempoPreparacion else 5
        faseActual = FaseSesion.CUENTA_ATRAS

        timerJob?.cancel()

        timerJob = viewModelScope.launch {
            while (conteoRegresivo > 0) {
                delay(1000)
                conteoRegresivo--
            }

            empezarGrabacionReal()
        }
    }

    private fun empezarGrabacionReal() {
        val ej = getEjercicioActual() ?: return
        segundosRestantes = ej.duracionSegundos
        faseActual = FaseSesion.GRABANDO

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (segundosRestantes > 0) {
                delay(1000)
                segundosRestantes--
            }
        }
    }

    /**
     * Permite al usuario detener la grabación antes de que se agote el tiempo establecido.
     */
    fun finalizarGrabacionAnticipada() {
        terminarGrabacion()
    }

    private fun terminarGrabacion() {
        timerJob?.cancel()
    }

    /**
     * Callback que se ejecuta cuando la grabación ha finalizado y el archivo de vídeo está listo para revisión.
     */
    fun onVideoRecorded(path: String) {
        timerJob?.cancel()
        rutaVideoGrabado = path
        faseActual = FaseSesion.REVIEW_EJERCICIO
    }

    /**
     * Permite descartar el vídeo actual y volver a intentar el ejercicio.
     */
    fun repetirEjercicioActual() {
        rutaVideoGrabado = null
        faseActual = FaseSesion.PREPARACION
    }

    /**
     * Valida el ejercicio realizado enviando el vídeo al servidor y avanzando al siguiente paso de la rutina.
     */
    fun confirmarYAvanzar() {
        val ruta = rutaVideoGrabado
        if (ruta == null) {
            errorMessage = "No hay vídeo grabado"
            return
        }

        val idSesion = sesionActual?.id ?: return
        val ejercicioActual = getEjercicioActual() ?: return

        faseActual = FaseSesion.GUARDANDO

        viewModelScope.launch {
            try {
                val body = SubirVideo(videoUrl = ruta)
                val response = RetrofitClient.instance.subirVideoEjercicio(
                    idSesion = idSesion,
                    idEjercicio = ejercicioActual.idEjercicio,
                    token = "Bearer $currentToken",
                    body = body
                )

                if (response.isSuccessful) {
                    avanzarIndice()
                } else {
                    errorMessage = "Error al subir vídeo (${response.code()})"
                    avanzarIndice()
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.message}"
                avanzarIndice()
            }
        }
    }

    private fun avanzarIndice() {
        if (indiceEjercicioActual < listaEjercicios.size - 1) {
            indiceEjercicioActual++
            rutaVideoGrabado = null
            faseActual = FaseSesion.PREPARACION
        } else {
            marcarSesionComoRealizada()
        }
    }

    private fun marcarSesionComoRealizada() {
        val idSesion = sesionActual?.id ?: return

        viewModelScope.launch {
            try {
                val datosUpdate = SesionUpdate(estado = "realizada")
                RetrofitClient.instance.updateSesion(
                    idSesion,
                    "Bearer $currentToken",
                    datosUpdate
                )

                faseActual = FaseSesion.RESUMEN_FINAL

            } catch (e: Exception) {
                faseActual = FaseSesion.RESUMEN_FINAL
            }
        }
    }
}