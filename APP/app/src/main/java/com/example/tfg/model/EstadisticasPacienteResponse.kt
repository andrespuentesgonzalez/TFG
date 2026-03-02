/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Modelos para la visualización del progreso clínico y estadísticas del paciente.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Estructura que asocia una fecha con una puntuación para la generación de gráficas de evolución.
 */
data class PuntoGrafica(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("puntuacion") val puntuacion: Int
)

/**
 * Contiene el resumen estadístico del rendimiento del paciente incluyendo promedios y evolución histórica.
 */
data class EstadisticasPacienteResponse(
    @SerializedName("promedio_puntuacion") val promedioPuntuacion: Double,
    @SerializedName("total_sesiones_completadas") val totalSesiones: Int,
    @SerializedName("maxima_puntuacion") val maximaPuntuacion: Int,
    @SerializedName("evolucion") val evolucion: List<PuntoGrafica> = emptyList()
)