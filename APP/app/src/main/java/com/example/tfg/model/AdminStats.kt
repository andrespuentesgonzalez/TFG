/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Modelos de datos para el Dashboard de Administrador.
 * Representan las estadísticas globales y los registros de actividad del sistema.
 */

package com.example.tfg.model

import com.google.gson.annotations.SerializedName

/**
 * Representa un registro individual de actividad reciente en el sistema.
 */
data class LogReciente(
    val usuario: String,
    val accion: String,
    val tiempo: String,
    val tipo: String
)

/**
 * Modelo de transferencia de datos que agrupa todas las métricas globales para el panel de administración.
 */
data class AdminStats(

    val totalUsuarios: Int,
    val admins: Int,
    val terapeutas: Int,
    val pacientes: Int,

    val totalEjercicios: Int,
    val totalFichas: Int,
    val totalRutinas: Int,
    val totalSesiones: Int,

    val sesionesHoy: Int,
    val pendientes: Int,
    val notaMedia: Double,

    @SerializedName("grafica_semanal")
    val graficaSemanal: List<Int> = emptyList(),

    @SerializedName("logs_recientes")
    val logsRecientes: List<LogReciente> = emptyList()
)