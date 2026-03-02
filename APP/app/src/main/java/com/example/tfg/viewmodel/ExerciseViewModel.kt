/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel para la administración del catálogo de ejercicios. Soporta operaciones CRUD y gestión de archivos de vídeo.
 */

package com.example.tfg.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.Ejercicio
import com.example.tfg.model.EjercicioCreate
import com.example.tfg.model.EjercicioUpdate
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

/**
 * Gestiona la lista de ejercicios disponibles y controla el formulario para crear o editar ejercicios incluyendo la carga de vídeos.
 */
class ExerciseViewModel : ViewModel() {

    var listaEjercicios by mutableStateOf<List<Ejercicio>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var nombre by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var duracion by mutableStateOf("")
    var videoUrl by mutableStateOf("")
    var zona by mutableStateOf("Torso")

    val zonasDisponibles = listOf("Torso", "Cabeza", "Extremidades_Superiores", "Extremidades_Inferiores", "Cuerpo_Completo", "Cardio")

    var idEjercicioAEditar by mutableStateOf<Int?>(null)

    /**
     * Recupera el listado completo de ejercicios desde el servidor.
     */
    fun cargarEjercicios(token: String) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.getEjercicios("Bearer $token")
                if (response.isSuccessful) {
                    listaEjercicios = response.body() ?: emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Error al cargar"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Restablece los campos del formulario a sus valores predeterminados para una nueva creación.
     */
    fun limpiarFormulario() {
        nombre = ""
        descripcion = ""
        duracion = ""
        videoUrl = ""
        zona = "Torso"
        idEjercicioAEditar = null
    }

    /**
     * Carga los datos de un ejercicio existente en el formulario para proceder a su edición.
     */
    fun prepararEdicion(ejercicio: Ejercicio) {
        idEjercicioAEditar = ejercicio.id
        nombre = ejercicio.nombre ?: ""
        descripcion = ejercicio.descripcion ?: ""
        duracion = ejercicio.duracion?.toString() ?: ""
        videoUrl = ejercicio.videoUrl ?: ""
        zona = ejercicio.zona ?: "Torso"
    }

    /**
     * Copia el archivo de vídeo seleccionado desde el almacenamiento externo al almacenamiento interno de la aplicación para su persistencia.
     */
    fun copyVideoToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileName = "VIDEO_LIB_${System.currentTimeMillis()}.mp4"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Determina si se debe crear o actualizar un ejercicio y envía la petición correspondiente a la API.
     */
    fun guardar(token: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            try {
                val duracionInt = duracion.toIntOrNull() ?: 0
                val videoFinal = if (videoUrl.isBlank()) null else videoUrl

                if (idEjercicioAEditar == null) {
                    val nuevo = EjercicioCreate(nombre, descripcion, duracionInt, videoFinal, zona)
                    val res = RetrofitClient.instance.createEjercicio("Bearer $token", nuevo)
                    if (res.isSuccessful) {
                        cargarEjercicios(token)
                        limpiarFormulario()
                        onFinished()
                    }
                } else {
                    val update = EjercicioUpdate(nombre, descripcion, duracionInt, videoFinal, zona)
                    val res = RetrofitClient.instance.updateEjercicio(idEjercicioAEditar!!, "Bearer $token", update)
                    if (res.isSuccessful) {
                        cargarEjercicios(token)
                        limpiarFormulario()
                        onFinished()
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error guardando"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Elimina un ejercicio del catálogo y actualiza la lista local.
     */
    fun eliminarEjercicio(token: String, id: Int) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.instance.deleteEjercicio(id, "Bearer $token")
                if (res.isSuccessful) listaEjercicios = listaEjercicios.filter { it.id != id }
            } catch (e: Exception) {}
        }
    }
}