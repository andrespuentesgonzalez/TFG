/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Clases de datos para la planificación, ejecución y evaluación de sesiones de rehabilitación.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Representa una cita o sesión de rehabilitación programada para un paciente.
 */
data class Sesion(
    @SerializedName("id_sesion") val id: Int,
    @SerializedName("fecha") val fecha: String?,
    @SerializedName("estado") val estado: String?,
    @SerializedName("id_paciente") val idPaciente: Int,
    @SerializedName("id_terapeuta") val idTerapeuta: Int,
    @SerializedName("id_rutina") val idRutina: Int?,
    @SerializedName("video_sesion") val videoSesion: String?,
    @SerializedName("comentario_paciente") val comentarioPaciente: String?,
    @SerializedName("puntuacion") val puntuacion: Int?,
    @SerializedName("comentario_terapeuta") val comentarioTerapeuta: String?,
    @SerializedName("tiempo_preparacion") val tiempoPreparacion: Int?,
    @SerializedName(value="ejercicios_detalles") val ejerciciosDetalles: List<SesionEjercicio>? = null
)

/**
 * Datos necesarios para programar una nueva sesión en el calendario.
 */
data class SesionCreate(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("id_paciente") val idPaciente: Int,
    @SerializedName("id_rutina") val idRutina: Int?,
    @SerializedName("tiempo_preparacion") val tiempoPreparacion: Int
)

/**
 * Objeto para actualizar el estado, comentarios o resultados de una sesión.
 */
data class SesionUpdate(
    @SerializedName("id_rutina") val idRutina: Int? = null,
    @SerializedName("fecha") val fecha: String? = null,
    @SerializedName("estado") val estado: String? = null,
    @SerializedName("video_sesion") val videoSesion: String? = null,
    @SerializedName("puntuacion") val puntuacion: Int? = null,
    @SerializedName("comentario_terapeuta") val comentarioTerapeuta: String? = null
)

/**
 * Contenedor para enviar la URL del vídeo grabado durante el ejercicio.
 */
data class SubirVideo(
    @SerializedName("video_url") val videoUrl: String
)

/**
 * Detalle de un ejercicio específico realizado dentro de una sesión.
 */
data class SesionEjercicio(
    @SerializedName("id") val id: Int,
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    @SerializedName("video_url") val videoUrl: String?
)