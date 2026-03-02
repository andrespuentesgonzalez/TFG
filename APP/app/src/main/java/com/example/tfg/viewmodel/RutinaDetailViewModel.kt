/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel para la gestión detallada de rutinas. Permite añadir y eliminar ejercicios dentro de una rutina específica.
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
 * Maneja el estado y las operaciones de modificación sobre una rutina concreta seleccionada por el terapeuta.
 */
class RutinaDetailViewModel : ViewModel() {

    var rutina by mutableStateOf<RutinaCompleta?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    var listaTodosEjercicios by mutableStateOf<List<Ejercicio>>(emptyList())
    var ejercicioSeleccionado by mutableStateOf<Ejercicio?>(null)

    /**
     * Carga la información completa de la rutina y el catálogo de ejercicios disponibles para posibles modificaciones.
     */
    fun cargarDatos(token: String, idRutina: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {

                val responseRutina = RetrofitClient.instance.getRutinaCompleta(idRutina, "Bearer $token")
                if (responseRutina.isSuccessful) rutina = responseRutina.body()

                val responseEjercicios = RetrofitClient.instance.getEjercicios("Bearer $token")
                if (responseEjercicios.isSuccessful) listaTodosEjercicios = responseEjercicios.body() ?: emptyList()

            } catch (e: Exception) {
                errorMessage = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Asocia un nuevo ejercicio a la rutina actual creando la ficha correspondiente en el servidor.
     */
    fun anadirEjercicioARutina(token: String, idRutina: Int, onSuccess: () -> Unit) {
        val ej = ejercicioSeleccionado ?: return

        viewModelScope.launch {
            isLoading = true
            try {
                val duracion = if ((ej.duracion ?: 0) > 0) ej.duracion!! else 5

                val ficha = FichaCreate(
                    idRutina = idRutina,
                    idEjercicio = ej.id,
                    series = 3,
                    repeticiones = 10,
                    tiempoMinutos = duracion
                )

                val response = RetrofitClient.instance.createFicha("Bearer $token", ficha)

                if (response.isSuccessful) {
                    cargarDatos(token, idRutina)
                    ejercicioSeleccionado = null
                    onSuccess()
                } else {
                    errorMessage = "Error ${response.code()}. Inténtalo de nuevo."
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Elimina un ejercicio de la rutina borrando su ficha asociada y actualiza la vista.
     */
    fun quitarEjercicioDeRutina(token: String, idFicha: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteFicha(idFicha, "Bearer $token")
                if (response.isSuccessful) {
                    rutina?.let { cargarDatos(token, it.id) }
                }
            } catch (e: Exception) {
            }
        }
    }
}