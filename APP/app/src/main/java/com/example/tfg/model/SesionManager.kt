/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Clase utilitaria para la gestión de la sesión del usuario mediante SharedPreferences.
 */

package com.example.tfg.model

import android.content.Context

/**
 * Gestor de preferencias compartidas para almacenar la sesión del usuario localmente.
 */
object SessionManager {

    private const val PREF_NAME = "AppParkinsonPrefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_ROL = "user_rol"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_NOMBRE = "user_nombre"

    /**
     * Guarda las credenciales y datos básicos del usuario tras el login.
     */
    fun saveSession(context: Context, token: String, rol: String, id: Int, nombre: String) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_ROL, rol)
            .putInt(KEY_USER_ID, id)
            .putString(KEY_NOMBRE, nombre)
            .apply()
    }

    /**
     * Recupera el token de autenticación almacenado.
     */
    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }

    /**
     * Obtiene el rol del usuario actual.
     */
    fun getRol(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_ROL, null)
    }

    /**
     * Devuelve el identificador único del usuario.
     */
    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_ID, -1)
    }

    /**
     * Recupera el nombre de pila del usuario.
     */
    fun getNombre(context: Context): String? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_NOMBRE, null)
    }

    /**
     * Borra todos los datos de sesión para cerrar la cuenta.
     */
    fun clearSession(context: Context) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}