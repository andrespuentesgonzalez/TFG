/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel para la pantalla de edición de usuarios que maneja la carga inicial de datos y la actualización de los mismos.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.UpdateUserRequest
import kotlinx.coroutines.launch

/**
 * Controlador de la lógica de negocio para modificar la información de perfil de un usuario existente.
 */
class EditUserViewModel : ViewModel() {

    var dni by mutableStateOf("")
    var nombre by mutableStateOf("")
    var apellidos by mutableStateOf("")
    var correo by mutableStateOf("")
    var rol by mutableStateOf("paciente")

    var isLoading by mutableStateOf(true)
    var statusMessage by mutableStateOf("")
    var updateSuccess by mutableStateOf(false)

    /**
     * Obtiene la información actual del usuario desde el servidor para rellenar los campos del formulario.
     */
    fun cargarDatosUsuario(token: String, userId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getUserById(userId, "Bearer $token")
                if (response.isSuccessful) {
                    val usuario = response.body()
                    if (usuario != null) {
                        dni = usuario.dni.toString()
                        nombre = usuario.nombre
                        apellidos = usuario.apellidos
                        correo = usuario.correo
                        rol = usuario.rol
                    }
                } else {
                    statusMessage = "Error al cargar usuario: ${response.code()}"
                }
            } catch (e: Exception) {
                statusMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Envía los datos modificados al servidor para actualizar el registro del usuario.
     */
    fun guardarCambios(token: String, userId: Int) {
        viewModelScope.launch {
            isLoading = true
            statusMessage = "Guardando..."
            try {
                val datosActualizados = UpdateUserRequest(
                    dni = dni.toLongOrNull() ?: 0,
                    nombre = nombre,
                    apellidos = apellidos,
                    correo = correo,
                    rol = rol
                )

                val response = RetrofitClient.instance.updateUser(userId, "Bearer $token", datosActualizados)

                if (response.isSuccessful) {
                    statusMessage = "¡Usuario modificado con éxito!"
                    updateSuccess = true
                } else {
                    statusMessage = "Error al guardar: ${response.code()}"
                }
            } catch (e: Exception) {
                statusMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}