/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel encargado de gestionar el estado y la lógica de negocio para el formulario de creación de nuevos usuarios.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.CreateUserRequest
import kotlinx.coroutines.launch

/**
 * Gestor de estado para el registro de usuarios que valida los campos de entrada y comunica la creación a la API.
 */
class CreateUserViewModel : ViewModel() {

    var dni by mutableStateOf("")
    var correo by mutableStateOf("")
    var nombre by mutableStateOf("")
    var apellidos by mutableStateOf("")
    var password by mutableStateOf("")

    var rol by mutableStateOf("paciente")
    var statusMessage by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var userCreatedSuccess by mutableStateOf(false)

    /**
     * Valida los datos introducidos y envía una petición al servidor para registrar un nuevo usuario en el sistema.
     * @param tokenAdmin Token de autenticación del administrador que realiza la operación.
     */
    fun crearUsuario(tokenAdmin: String) {
        if (dni.isEmpty() || password.isEmpty() || nombre.isEmpty() || correo.isEmpty()) {
            statusMessage = "Faltan datos obligatorios"
            return
        }

        val dniNumero = dni.toLongOrNull()
        if (dniNumero == null) {
            statusMessage = "El DNI debe ser numérico"
            return
        }

        viewModelScope.launch {
            isLoading = true
            statusMessage = "Enviando datos..."

            try {
                val nuevoUsuario = CreateUserRequest(
                    dni = dniNumero,
                    correoElectronico = correo,
                    nombre = nombre,
                    apellidos = apellidos,
                    rol = rol,
                    contrasena = password
                )

                val tokenHeader = "Bearer $tokenAdmin"

                val response = RetrofitClient.instance.createUser(tokenHeader, nuevoUsuario)

                if (response.isSuccessful) {
                    statusMessage = "¡Usuario creado con éxito!"
                    userCreatedSuccess = true
                    dni = ""
                    correo = ""
                    nombre = ""
                    apellidos = ""
                    password = ""
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                    statusMessage = "Error ${response.code()}: $errorBody"
                }

            } catch (e: Exception) {
                statusMessage = "Fallo de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}