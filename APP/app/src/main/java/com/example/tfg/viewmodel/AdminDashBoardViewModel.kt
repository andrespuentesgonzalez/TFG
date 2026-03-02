/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel encargado de gestionar el estado y la lógica de negocio para el panel de control del administrador.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.AdminStats
import kotlinx.coroutines.launch

/**
 * Gestiona la carga y presentación de las métricas globales del sistema para la vista del administrador.
 */
class AdminDashboardViewModel : ViewModel() {

    /**
     * Almacena el objeto con las estadísticas recibidas desde el servidor.
     */
    var stats by mutableStateOf<AdminStats?>(null)

    /**
     * Indica si hay una operación de carga de datos en curso.
     */
    var isLoading by mutableStateOf(false)

    /**
     * Contiene el mensaje descriptivo en caso de que ocurra un error durante la petición.
     */
    var errorMessage by mutableStateOf("")

    /**
     * Realiza una llamada asíncrona a la API para obtener los datos actualizados del dashboard utilizando el token de autenticación proporcionado.
     */
    fun cargarEstadisticas(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {

                val response = RetrofitClient.instance.getDashboardStats("Bearer $token")

                if (response.isSuccessful) {
                    stats = response.body()
                } else {
                    errorMessage = "Error al cargar datos: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}