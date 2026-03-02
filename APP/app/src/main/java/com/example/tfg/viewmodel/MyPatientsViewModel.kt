/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel para la gestión de la cartera de pacientes del terapeuta. Controla la lista de usuarios y la consulta de sus próximas sesiones.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.RutinaCompleta
import com.example.tfg.model.Sesion
import com.example.tfg.model.Usuario
import kotlinx.coroutines.launch

/**
 * Controla el estado de la pantalla de mis pacientes permitiendo cargar el listado y consultar detalles específicos de la agenda de cada uno.
 */
class MyPatientsViewModel : ViewModel() {

    var listaPacientes by mutableStateOf<List<Usuario>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    var siguienteSesion by mutableStateOf<Sesion?>(null)

    var rutinaAsignada by mutableStateOf<RutinaCompleta?>(null)

    var showSesionDialog by mutableStateOf(false)
    var mensajeSesion by mutableStateOf("")

    /**
     * Recupera del servidor la lista de pacientes asignados al terapeuta autenticado.
     */
    fun cargarMisPacientes(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val response = RetrofitClient.instance.getMisPacientes("Bearer $token")
                if (response.isSuccessful) {
                    listaPacientes = response.body() ?: emptyList()
                } else {
                    errorMessage = "Error al cargar pacientes: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                if (!showSesionDialog) isLoading = false
            }
        }
    }

    /**
     * Consulta la próxima sesión programada para un paciente específico y carga los detalles de la rutina asociada si existe.
     */
    fun cargarSiguienteSesion(token: String, idPaciente: Int) {
        viewModelScope.launch {
            isLoading = true
            siguienteSesion = null
            rutinaAsignada = null
            mensajeSesion = ""

            try {
                val response = RetrofitClient.instance.getSiguienteSesion(idPaciente, "Bearer $token")

                if (response.isSuccessful) {
                    val sesionEncontrada = response.body()
                    siguienteSesion = sesionEncontrada
                    if (sesionEncontrada?.idRutina != null) {
                        val responseRutina = RetrofitClient.instance.getRutinaCompleta(
                            sesionEncontrada.idRutina!!,
                            "Bearer $token"
                        )
                        if (responseRutina.isSuccessful) {
                            rutinaAsignada = responseRutina.body()
                        }
                    }

                    showSesionDialog = true

                } else if (response.code() == 404) {
                    mensajeSesion = "Este paciente no tiene sesiones programadas."
                    showSesionDialog = true
                } else {
                    mensajeSesion = "Error al consultar: ${response.code()}"
                    showSesionDialog = true
                }
            } catch (e: Exception) {
                mensajeSesion = "Error de conexión."
                showSesionDialog = true
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Restablece el estado del diálogo de información de sesión cerrándolo y limpiando los datos temporales.
     */
    fun cerrarDialogoSesion() {
        showSesionDialog = false
        siguienteSesion = null
        rutinaAsignada = null
        mensajeSesion = ""
    }
}