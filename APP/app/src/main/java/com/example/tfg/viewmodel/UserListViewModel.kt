/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: ViewModel para la pantalla de administración de usuarios. Permite listar, filtrar, eliminar y asignar relaciones terapeuta-paciente.
 */

package com.example.tfg.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tfg.api.RetrofitClient
import com.example.tfg.model.Usuario
import com.example.tfg.model.AsignarTerapeutaRequest
import kotlinx.coroutines.launch

/**
 * Gestiona la lista global de usuarios y las operaciones administrativas sobre ellos como filtrado por rol y asignación de terapeutas.
 */
class UserListViewModel : ViewModel() {

    private var listaCompleta: List<Usuario> = emptyList()
    var usuariosVisibles by mutableStateOf<List<Usuario>>(emptyList())
    var filtroActual by mutableStateOf("todos")

    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")

    /**
     * Descarga la lista completa de usuarios del sistema y aplica el filtro seleccionado inicialmente.
     */
    fun cargarUsuarios(token: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = ""
            try {
                val response = RetrofitClient.instance.getUsers("Bearer $token")
                if (response.isSuccessful) {
                    listaCompleta = response.body() ?: emptyList()
                    aplicarFiltro(filtroActual)
                } else {
                    errorMessage = "Error: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Filtra la lista de usuarios mostrada en pantalla según el criterio especificado.
     */
    fun aplicarFiltro(tipo: String) {
        filtroActual = tipo
        usuariosVisibles = when (tipo) {
            "todos" -> listaCompleta
            "asignaciones" -> listaCompleta.filter { it.rol == "paciente" && it.idTerapeuta == null }
            else -> listaCompleta.filter { it.rol.equals(tipo, ignoreCase = true) }
        }
    }

    /**
     * Elimina permanentemente un usuario del sistema y actualiza la lista local.
     */
    fun eliminarUsuario(token: String, usuario: Usuario) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitClient.instance.deleteUser(usuario.id, "Bearer $token")
                if (response.isSuccessful) {
                    listaCompleta = listaCompleta.filter { it.id != usuario.id }
                    aplicarFiltro(filtroActual)
                } else {
                    errorMessage = "No se pudo eliminar"
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Obtiene el nombre completo del terapeuta asignado a partir de su ID.
     */
    fun obtenerNombreTerapeuta(id: Int?): String {
        if (id == null) return "Sin asignar"
        val terapeuta = listaCompleta.find { it.id == id }
        return if (terapeuta != null)
            "${terapeuta.nombre} ${terapeuta.apellidos}"
        else
            "Terapeuta desconocido (ID: $id)"
    }

    /**
     * Devuelve una lista de pacientes que aún no tienen un terapeuta asignado.
     */
    fun obtenerPacientesSinAsignar(): List<Usuario> {
        return listaCompleta.filter {
            it.rol == "paciente" && it.idTerapeuta == null
        }
    }

    /**
     * Devuelve una lista con todos los usuarios que tienen el rol de terapeuta.
     */
    fun obtenerTerapeutas(): List<Usuario> {
        return listaCompleta.filter { it.rol == "terapeuta" }
    }

    /**
     * Establece la relación entre un paciente y un terapeuta en el sistema.
     */
    fun asignarTerapeuta(token: String, idPaciente: Int, idTerapeuta: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val body = AsignarTerapeutaRequest(idTerapeuta = idTerapeuta)
                val response = RetrofitClient.instance.assignTherapist(idPaciente, "Bearer $token", body)

                if (response.isSuccessful) {
                    cargarUsuarios(token)
                } else {
                    errorMessage = "Error al asignar: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Fallo de conexión"
            } finally {
                isLoading = false
            }
        }
    }
}