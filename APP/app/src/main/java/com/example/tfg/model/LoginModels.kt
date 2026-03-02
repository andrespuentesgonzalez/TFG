/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Clases de datos para gestionar el intercambio de credenciales y tokens en la autenticación.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Encapsula las credenciales de acceso del usuario para realizar la petición de inicio de sesión.
 */
data class LoginRequest(
    @SerializedName("username") val dni: String,
    @SerializedName("password") val contrasena: String
)

/**
 * Respuesta del servidor tras una autenticación exitosa que incluye el token de seguridad y datos del usuario.
 */
data class LoginResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("usuario") val usuario: Usuario
)