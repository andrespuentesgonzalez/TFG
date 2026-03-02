/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Modelo utilizado para enviar las modificaciones de perfil de un usuario existente.
 */

package com.example.tfg.model
import com.google.gson.annotations.SerializedName

/**
 * Contiene los campos editables de un usuario para su actualización en la base de datos.
 */
data class UpdateUserRequest(
    @SerializedName("dni") val dni: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("correo_electronico") val correo: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("id_terapeuta_asignado") val idTerapeuta: Int? = null
)