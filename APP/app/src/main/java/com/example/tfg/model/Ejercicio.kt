/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Modelos de datos relacionados con la gestión de ejercicios de rehabilitación.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Representa la entidad completa de un ejercicio físico disponible en la biblioteca del sistema.
 */
data class Ejercicio(
    @SerializedName("id_ejercicio") val id: Int,
    @SerializedName("nombre") val nombre: String?,
    @SerializedName("descripcion") val descripcion: String?,

    @SerializedName("duracion") val duracion: Int?,

    @SerializedName("video_prueba") val videoUrl: String?,
    @SerializedName("zona_afectada") val zona: String?,
    @SerializedName("id_terapeuta") val idTerapeuta: Int?,
    @SerializedName("terapeuta_rel") val terapeuta: TerapeutaSimple?
)

/**
 * Objeto de transferencia de datos con la información necesaria para registrar un nuevo ejercicio.
 */
data class EjercicioCreate(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("duracion") val duracion: Int,
    @SerializedName("video_prueba") val videoUrl: String?,
    @SerializedName("zona_afectada") val zona: String
)

/**
 * Objeto utilizado para enviar las modificaciones de los detalles de un ejercicio existente.
 */
data class EjercicioUpdate(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("duracion") val duracion: Int,
    @SerializedName("video_prueba") val videoUrl: String?,
    @SerializedName("zona_afectada") val zona: String
)