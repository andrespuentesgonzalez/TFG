/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel que gestiona la lógica de negocio y el estado de la interfaz durante el proceso de autenticación de usuarios.
 */

package com.example.tfg.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.SessionManager
import kotlinx.coroutines.launch

/**
 * Gestiona los datos del formulario de acceso y realiza la comunicación con el servidor para validar las credenciales.
 */
class LoginViewModel : ViewModel() {

    var dni by mutableStateOf("")
    var password by mutableStateOf("")

    var loginStatus by mutableStateOf("Introduce tus datos")
    var isLoading by mutableStateOf(false)

    /**
     * Envía las credenciales al servidor y gestiona la respuesta almacenando la sesión en caso de éxito o mostrando errores.
     */
    fun login(context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            loginStatus = "Cargando..."

            try {
                val response = RetrofitClient.instance.login(dni, password)

                if (response.isSuccessful && response.body() != null) {
                    val loginData = response.body()!!

                    SessionManager.saveSession(
                        context = context,
                        token = loginData.accessToken,
                        rol = loginData.rol,
                        id = loginData.usuario.id,
                        nombre = loginData.usuario.nombre
                    )

                    loginStatus = "¡Éxito!"
                    isLoading = false
                    onSuccess()

                } else {
                    val code = response.code()
                    if (code == 401) {
                        loginStatus = "DNI o contraseña incorrectos."
                    } else {
                        loginStatus = "Error del servidor: $code"
                    }
                    isLoading = false
                }
            } catch (e: Exception) {
                loginStatus = "Error de conexión. Revisa tu internet."
                isLoading = false
            }
        }
    }
}