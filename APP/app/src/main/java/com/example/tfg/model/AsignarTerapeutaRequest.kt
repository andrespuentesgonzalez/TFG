/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Modelo de datos para la solicitud de asignación de un terapeuta a un paciente.
 */

package com.example.tfg.model
import com.google.gson.annotations.SerializedName

/**
 * Contiene el identificador del terapeuta que será asignado al paciente seleccionado.
 */
data class AsignarTerapeutaRequest(
    @SerializedName("id_terapeuta") val idTerapeuta: Int
)