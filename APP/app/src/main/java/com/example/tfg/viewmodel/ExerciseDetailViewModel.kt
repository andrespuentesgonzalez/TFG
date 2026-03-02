/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel que gestiona la vista detallada de una rutina permitiendo la adición y eliminación de ejercicios específicos.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.Ejercicio
import com.example.tfg.model.FichaCreate
import com.example.tfg.model.RutinaCompleta
import kotlinx.coroutines.launch

/**
 * Maneja el estado de la pantalla de detalles de rutina incluyendo la lista de ejercicios asignados y los disponibles para agregar.
 */
class ExerciseDetailViewModel : ViewModel() {

    var rutina by mutableStateOf<RutinaCompleta?>(null)
    var listaTodosEjercicios by mutableStateOf<List<Ejercicio>>(emptyList())
    var ejercicioSeleccionado by mutableStateOf<Ejercicio?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    /**
     * Carga tanto los detalles de la rutina actual como el catálogo completo de ejercicios disponibles.
     */
    fun cargarDatos(token: String, rutinaId: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val respDetalle = RetrofitClient.instance.getRutinaCompleta(rutinaId, "Bearer $token")
                val respEjercicios = RetrofitClient.instance.getEjercicios("Bearer $token")

                if (respDetalle.isSuccessful && respEjercicios.isSuccessful) {
                    rutina = respDetalle.body()
                    listaTodosEjercicios = respEjercicios.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Crea una nueva ficha de ejercicio asociada a la rutina actual y actualiza la vista tras el éxito.
     */
    fun anadirEjercicioARutina(token: String, rutinaId: Int, onSuccess: () -> Unit) {
        val ej = ejercicioSeleccionado ?: return
        viewModelScope.launch {
            try {
                val nuevaFicha = FichaCreate(
                    idRutina = rutinaId,
                    idEjercicio = ej.id,
                    series = 3,
                    repeticiones = 10,
                    tiempoMinutos = 5
                )

                val response = RetrofitClient.instance.createFicha("Bearer $token", nuevaFicha)
                if (response.isSuccessful) {
                    cargarDatos(token, rutinaId)
                    ejercicioSeleccionado = null
                    onSuccess()
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            }
        }
    }

    /**
     * Elimina un ejercicio específico de la rutina mediante su identificador de ficha.
     */
    fun quitarEjercicioDeRutina(token: String, fichaId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteFicha(fichaId, "Bearer $token")
                if (response.isSuccessful) {
                    cargarDatos(token, rutina?.id ?: return@launch)
                }
            } catch (e: Exception) { }
        }
    }
}