/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Entidad principal que representa a un usuario registrado en la aplicación.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Modelo de dominio que mapea la respuesta del servidor con los datos completos de un usuario.
 */
data class Usuario(
    @SerializedName("id_usuario") val id: Int,
    @SerializedName("dni") val dni: Long,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String,
    @SerializedName("correo_electronico") val correo: String,
    @SerializedName("rol") val rol: String,
    @SerializedName("id_terapeuta_asignado") val idTerapeuta: Int?
)