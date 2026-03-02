/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Modelos de datos para la gestión de rutinas de ejercicios y sus fichas asociadas.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Representa una rutina de ejercicios asignada a un tratamiento.
 */
data class Rutina(
    @SerializedName("id_rutina") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("id_terapeuta") val idTerapeuta: Int,
    @SerializedName("terapeuta_rel") val terapeuta: TerapeutaSimple?,
    @SerializedName("fichas") val fichas: List<FichaCompleta>? = emptyList()
)

/**
 * Información básica del terapeuta creador de la rutina.
 */
data class TerapeutaSimple(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("apellidos") val apellidos: String
)

/**
 * Datos de entrada para añadir un ejercicio a una rutina.
 */
data class FichaInput(
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    @SerializedName("series") val series: Int = 3,
    @SerializedName("repeticiones") val repeticiones: Int = 10,
    @SerializedName("tiempo_minutos") val tiempoMinutos: Int = 0
)

/**
 * Objeto para la creación de una ficha de ejercicio en la base de datos.
 */
data class FichaCreate(
    @SerializedName("id_rutina") val idRutina: Int,
    @SerializedName("id_ejercicio") val idEjercicio: Int,
    @SerializedName("series") val series: Int,
    @SerializedName("repeticiones") val repeticiones: Int,
    @SerializedName("tiempo_minutos") val tiempoMinutos: Int
)

/**
 * Estructura para registrar una nueva rutina con su lista de ejercicios inicial.
 */
data class RutinaCreate(
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("ejercicios") val ejercicios: List<FichaInput>
)

/**
 * Detalle de un ejercicio dentro de una rutina incluyendo sus parámetros específicos.
 */
data class FichaCompleta(
    @SerializedName("id_ficha_ejercicios") val id: Int,
    @SerializedName("id_ejercicio") val idEjercicio: Int?,

    @SerializedName("series") val series: Int?,
    @SerializedName("repeticiones") val repeticiones: Int?,
    @SerializedName("tiempo_minutos") val tiempoMinutos: Int?,
    @SerializedName("ejercicio_rel") val ejercicio: Ejercicio
)

/**
 * Vista detallada de una rutina que incluye la lista completa de ejercicios asociados.
 */
data class RutinaCompleta(
    @SerializedName("id_rutina") val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("descripcion") val descripcion: String?,
    @SerializedName("id_terapeuta") val idTerapeuta: Int,
    @SerializedName("fichas") val fichas: List<FichaCompleta>
)

/**
 * Modelo para modificar el nombre o descripción de una rutina existente.
 */
data class RutinaUpdate(
    @SerializedName("nombre") val nombre: String? = null,
    @SerializedName("descripcion") val descripcion: String? = null
)