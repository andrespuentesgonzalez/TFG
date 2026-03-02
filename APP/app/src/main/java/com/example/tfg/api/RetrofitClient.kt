/*
 * Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
 * Autor: Andres Puentes Gonzalez
 * Descripción: Configuración del cliente HTTP (Retrofit). Implementa el patrón Singleton para gestionar la conexión con la API.
 */

package com.example.tfg.api

import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Objeto Singleton que centraliza la configuración de red.
 * Se encarga de crear y proveer una única instancia de la interfaz [ApiService] para toda la aplicación.
 */
object RetrofitClient {

    // Dirección IP del servidor backend (Cambiar según el entorno de red)
    // IP de pruebas uni
    // private const val BASE_URL = "http://192.168.1.58:8000/"

    // IP de movil para pruebas
    private const val BASE_URL = "http://172.20.10.2:8000/"

    // IP
    //private const val BASE_URL = "http://10.0.0.0:8000/"

    // Configuración del serializador JSON (GSON) para manejar formatos flexibles y valores nulos
    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }
}