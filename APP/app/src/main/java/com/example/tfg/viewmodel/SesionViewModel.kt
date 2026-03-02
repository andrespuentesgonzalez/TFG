/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel principal para la administración de sesiones. Gestiona la planificación, asignación, evaluación y seguimiento de las citas terapéuticas.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.Ejercicio
import com.example.tfg.model.Rutina
import com.example.tfg.model.Sesion
import com.example.tfg.model.SesionCreate
import com.example.tfg.model.SesionUpdate
import com.example.tfg.model.Usuario
import com.example.tfg.model.SubirVideo
import kotlinx.coroutines.launch

/**
 * Coordina todas las operaciones relacionadas con las sesiones de rehabilitación incluyendo su creación, modificación, borrado y calificación.
 */
class SesionViewModel : ViewModel() {

    var listaSesiones by mutableStateOf<List<Sesion>>(emptyList())

    var listaPacientes by mutableStateOf<List<Usuario>>(emptyList())
    var listaRutinas by mutableStateOf<List<Rutina>>(emptyList())

    var listaEjercicios by mutableStateOf<List<Ejercicio>>(emptyList())

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    var fechaSeleccionada by mutableStateOf("")
    var pacienteSeleccionado by mutableStateOf<Usuario?>(null)
    var rutinaSeleccionada by mutableStateOf<Rutina?>(null)

    var tiempoPreparacionSeleccionado by mutableIntStateOf(10)

    /**
     * Carga todos los datos necesarios para el panel del terapeuta incluyendo sesiones, pacientes asignados y rutinas disponibles.
     */
    fun cargarDatosTerapeuta(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val resSesiones = RetrofitClient.instance.getSesionesTerapeuta("Bearer $token")
                if (resSesiones.isSuccessful) listaSesiones = resSesiones.body() ?: emptyList()
                val resPacientes = RetrofitClient.instance.getMisPacientes("Bearer $token")
                if (resPacientes.isSuccessful) listaPacientes = resPacientes.body() ?: emptyList()
                val resRutinas = RetrofitClient.instance.getRutinas("Bearer $token")
                if (resRutinas.isSuccessful) listaRutinas = resRutinas.body() ?: emptyList()
                val resEjercicios = RetrofitClient.instance.getEjercicios("Bearer $token")
                if (resEjercicios.isSuccessful) listaEjercicios = resEjercicios.body() ?: emptyList()

            } catch (e: Exception) {
                errorMessage = "Error cargando datos: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }


    /**
     * Recupera el nombre completo de un paciente a partir de su ID para mostrarlo en listados y detalles.
     */
    fun obtenerNombrePaciente(id: Int): String {
        return listaPacientes.find { it.id == id }?.let { "${it.nombre} ${it.apellidos}" } ?: "Paciente ID: $id"
    }

    /**
     * Recupera el nombre de una rutina a partir de su ID manejando casos nulos.
     */
    fun obtenerNombreRutina(id: Int?): String {
        if (id == null) return "Sin rutina"
        return listaRutinas.find { it.id == id }?.nombre ?: "Rutina ID: $id"
    }

    /**
     * Registra una nueva sesión en el sistema validando los datos obligatorios antes del envío.
     */
    fun crearSesion(token: String, onSuccess: () -> Unit) {
        if (pacienteSeleccionado == null || fechaSeleccionada.isEmpty()) {
            errorMessage = "Faltan datos obligatorios"
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val nueva = SesionCreate(
                    fecha = fechaSeleccionada,
                    idPaciente = pacienteSeleccionado!!.id,
                    idRutina = rutinaSeleccionada?.id,
                    tiempoPreparacion = tiempoPreparacionSeleccionado
                )

                val response = RetrofitClient.instance.createSesion("Bearer $token", nueva)
                if (response.isSuccessful) {
                    cargarDatosTerapeuta(token)
                    limpiarFormulario()
                    onSuccess()
                } else {
                    errorMessage = "Error al crear: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Restablece los campos del formulario de creación de sesión a sus valores iniciales.
     */
    fun limpiarFormulario() {
        fechaSeleccionada = ""
        pacienteSeleccionado = null
        rutinaSeleccionada = null
        tiempoPreparacionSeleccionado = 10
        errorMessage = ""
    }

    /**
     * Actualiza una sesión existente con la puntuación y comentarios del terapeuta marcándola como corregida.
     */
    fun evaluarSesion(token: String, idSesion: Int, puntuacion: Int, comentario: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val update = SesionUpdate(
                    puntuacion = puntuacion,
                    comentarioTerapeuta = comentario,
                    estado = "corregida"
                )

                val response = RetrofitClient.instance.updateSesion(idSesion, "Bearer $token", update)
                if (response.isSuccessful) {
                    cargarDatosTerapeuta(token)
                    onSuccess()
                } else {
                    errorMessage = "Error al guardar nota: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun getSesionPorId(id: Int): Sesion? {
        return listaSesiones.find { it.id == id }
    }

    /**
     * Elimina una sesión programada del sistema.
     */
    fun eliminarSesion(token: String, idSesion: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.deleteSesion(idSesion, "Bearer $token")
                if (response.isSuccessful) {
                    cargarDatosTerapeuta(token)
                    onSuccess()
                } else {
                    errorMessage = "Error al eliminar: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Modifica la fecha y hora de una sesión existente permitiendo su reprogramación.
     */
    fun reprogramarSesion(token: String, idSesion: Int, nuevaFechaIso: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val update = SesionUpdate(fecha = nuevaFechaIso)

                val response = RetrofitClient.instance.updateSesion(idSesion, "Bearer $token", update)

                if (response.isSuccessful) {
                    cargarDatosTerapeuta(token)
                    onSuccess()
                } else {
                    errorMessage = "Error al reprogramar: ${response.code()}"
                }

            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Sube un archivo de vídeo correspondiente a un ejercicio realizado en una sesión específica.
     */
    fun subirVideoDeEjercicio(token: String, idSesion: Int, idEjercicio: Int, rutaVideo: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val body = SubirVideo(videoUrl = rutaVideo)
                val response = RetrofitClient.instance.subirVideoEjercicio(
                    idSesion = idSesion,
                    idEjercicio = idEjercicio,
                    token = "Bearer $token",
                    body = body
                )

                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    errorMessage = "Error subiendo video: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de red: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}