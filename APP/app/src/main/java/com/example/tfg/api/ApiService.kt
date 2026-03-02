/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Interfaz de Retrofit para la comunicación con la API REST.
 * Define los endpoints HTTP, los métodos de solicitud y los modelos de respuesta.
 */

package com.example.tfg.api

import com.example.tfg.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==========================================
    // LOGIN
    // ==========================================

    /**
     * Autentica al usuario mediante DNI y contraseña para obtener el token de acceso.
     */
    @FormUrlEncoded
    @POST("token")
    suspend fun login(
        @Field("username") dni: String,
        @Field("password") contrasena: String
    ): Response<LoginResponse>

    // ==========================================
    // USUARIOS
    // ==========================================

    /**
     * Crea un nuevo usuario en el sistema (Requiere permisos de Administrador).
     */
    @POST("usuarios")
    suspend fun createUser(
        @Header("Authorization") token: String,
        @Body usuario: CreateUserRequest
    ): Response<Usuario>

    /**
     * Obtiene el listado completo de usuarios registrados (Administrador).
     */
    @GET("usuarios")
    suspend fun getUsers(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    /**
     * Elimina un usuario del sistema por su ID.
     */
    @DELETE("usuarios/{id}")
    suspend fun deleteUser(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>

    /**
     * Actualiza los datos de un usuario existente.
     */
    @PUT("usuarios/{id}")
    suspend fun updateUser(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body usuario: UpdateUserRequest
    ): Response<Usuario>

    /**
     * Obtiene los detalles de un usuario específico por su ID.
     */
    @GET("usuarios/{id}")
    suspend fun getUserById(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Usuario>

    /**
     * Asigna un terapeuta responsable a un paciente específico.
     */
    @PATCH("usuarios/{id_paciente}/asignar-terapeuta")
    suspend fun assignTherapist(
        @Path("id_paciente") idPaciente: Int,
        @Header("Authorization") token: String,
        @Body body: AsignarTerapeutaRequest
    ): Response<Usuario>

    /**
     * Lista todos los pacientes asignados al terapeuta logueado.
     */
    @GET("usuarios/mis-pacientes")
    suspend fun getMisPacientes(
        @Header("Authorization") token: String
    ): Response<List<Usuario>>

    /**
     * Obtiene estadísticas de desempeño y evolución de un paciente.
     */
    @GET("usuarios/{id}/estadisticas")
    suspend fun getPatientStats(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<EstadisticasPacienteResponse>

    /**
     * Obtiene el historial de sesiones de rehabilitación de un paciente.
     */
    @GET("usuarios/{id}/sesiones")
    suspend fun getPatientSessions(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<List<Sesion>>

    /**
     * Recupera la próxima sesión pendiente más cercana para el paciente.
     */
    @GET("usuarios/{id}/siguiente-sesion")
    suspend fun getSiguienteSesion(
        @Path("id") idPaciente: Int,
        @Header("Authorization") token: String
    ): Response<Sesion>

    // ==========================================
    // DASHBOARD (ADMIN)
    // ==========================================

    /**
     * Obtiene métricas globales del sistema para el panel de administración.
     */
    @GET("dashboard/stats")
    suspend fun getDashboardStats(
        @Header("Authorization") token: String
    ): Response<AdminStats>

    // ==========================================
    // EJERCICIOS
    // ==========================================

    /**
     * Obtiene el catálogo completo de ejercicios disponibles.
     */
    @GET("ejercicios")
    suspend fun getEjercicios(
        @Header("Authorization") token: String
    ): Response<List<Ejercicio>>

    /**
     * Crea un nuevo ejercicio en la biblioteca.
     */
    @POST("ejercicios")
    suspend fun createEjercicio(
        @Header("Authorization") token: String,
        @Body ejercicio: EjercicioCreate
    ): Response<Ejercicio>

    /**
     * Elimina un ejercicio de la base de datos.
     */
    @DELETE("ejercicios/{id}")
    suspend fun deleteEjercicio(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>

    /**
     * Actualiza la información de un ejercicio.
     */
    @PUT("ejercicios/{id}")
    suspend fun updateEjercicio(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body ejercicio: EjercicioUpdate
    ): Response<Ejercicio>

    // ==========================================
    // RUTINAS
    // ==========================================

    /**
     * Lista todas las rutinas de ejercicios disponibles.
     */
    @GET("rutinas")
    suspend fun getRutinas(
        @Header("Authorization") token: String
    ): Response<List<Rutina>>

    /**
     * Crea una nueva rutina y asigna los ejercicios correspondientes.
     */
    @POST("rutinas")
    suspend fun createRutina(
        @Header("Authorization") token: String,
        @Body rutina: RutinaCreate
    ): Response<Rutina>

    /**
     * Elimina una rutina completa.
     */
    @DELETE("rutinas/{id}")
    suspend fun deleteRutina(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>

    /**
     * Modifica los datos básicos de una rutina existente.
     */
    @PUT("rutinas/{id}")
    suspend fun updateRutina(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body rutina: RutinaCreate
    ): Response<Rutina>

    /**
     * Obtiene una rutina con todos sus ejercicios detallados (fichas).
     */
    @GET("rutinas/{id}")
    suspend fun getRutinaCompleta(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<RutinaCompleta>

    // ==========================================
    // FICHAS
    // ==========================================

    /**
     * Añade un ejercicio específico a una rutina (crea una ficha).
     */
    @POST("fichas")
    suspend fun createFicha(
        @Header("Authorization") token: String,
        @Body ficha: FichaCreate
    ): Response<Void>

    /**
     * Elimina un ejercicio (ficha) de una rutina.
     */
    @DELETE("fichas/{id}")
    suspend fun deleteFicha(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>

    // ==========================================
    // SESIONES
    // ==========================================

    /**
     * Lista todas las sesiones del sistema (vista Admin).
     */
    @GET("sesiones")
    suspend fun getSesiones(
        @Header("Authorization") token: String
    ): Response<List<Sesion>>

    /**
     * Obtiene las sesiones asociadas al terapeuta logueado.
     */
    @GET("sesiones/mias")
    suspend fun getSesionesTerapeuta(
        @Header("Authorization") token: String
    ): Response<List<Sesion>>

    /**
     * Obtiene los detalles de una sesión específica.
     */
    @GET("sesiones/{id}")
    suspend fun getSesion(
        @Header("Authorization") token: String,
        @Path("id") id: Int
    ): Response<Sesion>

    /**
     * Programa una nueva sesión de rehabilitación.
     */
    @POST("sesiones")
    suspend fun createSesion(
        @Header("Authorization") token: String,
        @Body sesion: SesionCreate
    ): Response<Sesion>

    /**
     * Actualiza el estado, puntuación o comentarios de una sesión.
     */
    @PUT("sesiones/{id}")
    suspend fun updateSesion(
        @Path("id") id: Int,
        @Header("Authorization") token: String,
        @Body datos: SesionUpdate
    ): Response<Sesion>

    /**
     * Cancela o elimina una sesión programada.
     */
    @DELETE("sesiones/{id}")
    suspend fun deleteSesion(
        @Path("id") id: Int,
        @Header("Authorization") token: String
    ): Response<Void>

    /**
     * Sube la URL del vídeo de un ejercicio realizado dentro de una sesión.
     */
    @PUT("sesiones/{id_sesion}/ejercicio/{id_ejercicio}")
    suspend fun subirVideoEjercicio(
        @Path("id_sesion") idSesion: Int,
        @Path("id_ejercicio") idEjercicio: Int,
        @Header("Authorization") token: String,
        @Body body: SubirVideo
    ): Response<Any>
}