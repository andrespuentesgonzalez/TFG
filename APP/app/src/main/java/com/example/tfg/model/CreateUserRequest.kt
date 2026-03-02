/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Estructura de datos requerida para el registro de un nuevo usuario en el sistema.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Objeto de transferencia de datos con la información necesaria para crear un usuario.
 */
data class CreateUserRequest(
    @SerializedName("dni") val dni: Long,
    @SerializedName("correo_electronico") val correoElectronico: String,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("contrasena") val contrasena: String
)