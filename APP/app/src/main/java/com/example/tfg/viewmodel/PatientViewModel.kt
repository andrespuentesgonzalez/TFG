/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel principal del paciente que gestiona la carga de la información de inicio y próximas sesiones.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.Sesion
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Controla la lógica de la pantalla de inicio del paciente identificando la próxima sesión pendiente más cercana.
 */
class PatientViewModel : ViewModel() {

    var proximaSesion by mutableStateOf<Sesion?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    /**
     * Consulta al servidor todas las sesiones disponibles y filtra localmente para encontrar la siguiente cita válida.
     */
    fun cargarProximaSesion(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {

                val response = RetrofitClient.instance.getSesiones("Bearer $token")

                if (response.isSuccessful) {
                    val todasLasSesiones = response.body() ?: emptyList()
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
                    val fechaActual = sdf.format(Date())
                    proximaSesion = todasLasSesiones
                        .filter { sesion ->
                            val estado = sesion.estado?.trim()?.lowercase() ?: ""
                            estado == "pendiente"
                        }
                        .filter { sesion ->
                            val fechaSesion = sesion.fecha ?: ""
                            fechaSesion >= fechaActual
                        }
                        .sortedBy { it.fecha }
                        .firstOrNull()


                } else {
                    errorMessage = "Error servidor: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}