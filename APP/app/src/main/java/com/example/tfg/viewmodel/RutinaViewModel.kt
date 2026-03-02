/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel encargado de la gestión de rutinas de ejercicios. Permite listar, crear, modificar y eliminar rutinas así como administrar los ejercicios asociados.
 */

package com.example.tfg.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.Ejercicio
import com.example.tfg.model.FichaInput
import com.example.tfg.model.Rutina
import com.example.tfg.model.RutinaCreate
import kotlinx.coroutines.launch

/**
 * Controla la lógica de negocio para la administración de rutinas facilitando la composición de planes de entrenamiento mediante la selección de ejercicios.
 */
class RutinaViewModel : ViewModel() {

    var listaRutinas by mutableStateOf<List<Rutina>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    var listaTodosEjercicios by mutableStateOf<List<Ejercicio>>(emptyList())

    var ejerciciosSeleccionados by mutableStateOf<List<FichaInput>>(emptyList())


    /**
     * Añade un ejercicio a la lista temporal de la rutina que se está creando especificando los parámetros de ejecución.
     */
    fun agregarEjercicioALista(idEjercicio: Int, series: Int, repes: Int, minutos: Int) {
        val nuevaFicha = FichaInput(
            idEjercicio = idEjercicio,
            series = series,
            repeticiones = repes,
            tiempoMinutos = minutos
        )
        ejerciciosSeleccionados = ejerciciosSeleccionados + nuevaFicha
    }

    /**
     * Elimina un ejercicio de la lista temporal de creación basándose en su posición.
     */
    fun quitarEjercicioDeLista(index: Int) {
        val nuevaLista = ejerciciosSeleccionados.toMutableList()
        if (index in nuevaLista.indices) {
            nuevaLista.removeAt(index)
            ejerciciosSeleccionados = nuevaLista
        }
    }

    /**
     * Restablece el formulario de creación de rutinas limpiando la lista de ejercicios seleccionados y los mensajes de error.
     */
    fun limpiarFormulario() {
        ejerciciosSeleccionados = emptyList()
        errorMessage = ""
    }


    /**
     * Recupera el listado completo de rutinas disponibles desde el servidor.
     */
    fun cargarRutinas(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val respRutinas = RetrofitClient.instance.getRutinas("Bearer $token")
                if (respRutinas.isSuccessful) {
                    listaRutinas = respRutinas.body() ?: emptyList()
                } else {
                    errorMessage = "Error al cargar rutinas: ${respRutinas.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Obtiene el catálogo de ejercicios existentes para permitir su selección durante la creación de rutinas.
     */
    fun cargarEjerciciosDisponibles(token: String) {
        viewModelScope.launch {
            try {
                val respEjercicios = RetrofitClient.instance.getEjercicios("Bearer $token")
                if (respEjercicios.isSuccessful) {
                    listaTodosEjercicios = respEjercicios.body() ?: emptyList()
                }
            } catch (e: Exception) {
                Log.e("RutinaVM", "Error cargando ejercicios: ${e.message}")
            }
        }
    }

    /**
     * Envía la solicitud de creación de una nueva rutina con los ejercicios seleccionados al servidor.
     */
    fun crearRutina(
        token: String,
        nombre: String,
        descripcion: String,
        onSuccess: () -> Unit
    ) {
        if (nombre.isBlank()) {
            errorMessage = "El nombre es obligatorio"
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val nuevaRutina = RutinaCreate(
                    nombre = nombre,
                    descripcion = descripcion,
                    ejercicios = ejerciciosSeleccionados
                )

                val response = RetrofitClient.instance.createRutina("Bearer $token", nuevaRutina)

                if (response.isSuccessful) {
                    cargarRutinas(token)
                    limpiarFormulario()
                    onSuccess()
                } else {
                    errorMessage = "Error al crear rutina: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Elimina una rutina específica del sistema y actualiza la lista local.
     */
    fun borrarRutina(token: String, id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.deleteRutina(id, "Bearer $token")
                if (response.isSuccessful) {
                    listaRutinas = listaRutinas.filter { it.id != id }
                } else {
                    errorMessage = "Error al eliminar"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Busca y devuelve el nombre de un ejercicio dado su identificador para mostrarlo en la interfaz.
     */
    fun nombreEjercicio(id: Int): String {
        return listaTodosEjercicios.find { it.id == id }?.nombre ?: "Ejercicio $id"
    }
}