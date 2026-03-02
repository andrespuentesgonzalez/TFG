/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel encargado de recuperar y organizar el historial clínico y de sesiones tanto para el propio paciente como para la revisión por parte del terapeuta.
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

/**
 * Gestiona la carga de datos históricos de sesiones para su visualización en listas o gráficas de evolución.
 */
class PatientHistoryViewModel : ViewModel() {

    var listaHistorial by mutableStateOf<List<Sesion>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    var nombrePaciente by mutableStateOf("")

    /**
     * Carga el historial de sesiones asociado al usuario actual basándose en su token de autenticación.
     */
    fun cargarHistorial(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val response = RetrofitClient.instance.getSesionesTerapeuta("Bearer $token")
                if (response.isSuccessful) {
                    val todas = response.body() ?: emptyList()
                    listaHistorial = todas.sortedByDescending { it.fecha }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Recupera el historial específico de un paciente mediante su ID permitiendo al terapeuta revisar su progreso.
     */
    fun cargarHistorialDePaciente(token: String, idPaciente: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {

                val responseSesiones = RetrofitClient.instance.getPatientSessions(idPaciente, "Bearer $token")
                if (responseSesiones.isSuccessful) {
                    val sesiones = responseSesiones.body() ?: emptyList()
                    listaHistorial = sesiones.sortedByDescending { it.fecha }
                }

                val responseUser = RetrofitClient.instance.getUserById(idPaciente, "Bearer $token")
                if (responseUser.isSuccessful) {
                    val user = responseUser.body()
                    if (user != null) {
                        nombrePaciente = "${user.nombre} ${user.apellidos}".uppercase()
                    }
                }

            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}