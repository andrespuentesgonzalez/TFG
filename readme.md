#  Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil

![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-7F52FF.svg?logo=kotlin)
![Android](https://img.shields.io/badge/Android-Jetpack_Compose-3DDC84.svg?logo=android)
![Python](https://img.shields.io/badge/Python-3.10+-blue.svg?logo=python)
![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688.svg?logo=fastapi)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791.svg?logo=postgresql)

Plataforma Full-Stack de telerrehabilitación asíncrona desarrollada como Trabajo de Fin de Grado (TFG). El ecosistema está compuesto por una **Aplicación Móvil Nativa (Android)** y un **Backend robusto (API REST)**, diseñados en conjunto para facilitar el seguimiento de pacientes con enfermedades neurodegenerativas como el Parkinson.

La solución permite a los terapeutas prescribir rutinas, y a los pacientes ejecutar y grabar su terapia desde casa, utilizando una interfaz adaptada para mitigar limitaciones motoras como la bradicinesia y los temblores.

## Características Principales

El proyecto se divide en dos grandes bloques tecnológicos que interactúan mediante una arquitectura Cliente-Servidor distribuida.

### Frontend (Aplicación Android Nativa)
* **Interfaz Adaptada y Accesible:** Diseño UI/UX construido de forma declarativa con Jetpack Compose. Implementa flujos de "mínima interacción" y botones sobredimensionados pensados específicamente para pacientes.
* **Captura y Reproducción Multimedia:** Integración directa con el hardware del dispositivo mediante CameraX para la grabación fluida de los ejercicios, y uso de ExoPlayer (Media3) para la reproducción de las guías visuales.
* **Arquitectura MVVM:** Separación limpia entre la interfaz gráfica y la lógica de presentación utilizando ViewModels, basándose en mecanismos de observación reactiva del estado.
* **Consumo de API Seguro:** Cliente HTTP configurado con Retrofit para la sincronización de datos y envío de archivos de vídeo al servidor.

### Backend (API REST)
* **Autenticación y Autorización (JWT):** Sistema de control de acceso basado en roles estrictos (Administrador, Terapeuta, Paciente) para garantizar la privacidad de los datos.
* **Gestión Multimedia Segura:** Recepción, almacenamiento en disco y servicio estático de archivos `.mp4` generados como evidencia durante las terapias.
* **Integridad Referencial Fuerte:** Esquema de base de datos relacional normalizado en PostgreSQL que evita anomalías (ej: borrado en cascada controlado para mantener el historial clínico intacto).
* **Procesamiento de Estadísticas:** Endpoints dedicados que calculan métricas complejas (notas medias, progresión) en el servidor, optimizando el consumo de batería y datos en el dispositivo móvil.

## Stack Tecnológico

**Cliente (Móvil):**
* **Lenguaje:** Kotlin
* **UI:** Jetpack Compose, Material Design 3
* **Arquitectura & Asincronía:** MVVM, Kotlin Coroutines
* **Librerías clave:** CameraX, ExoPlayer, Retrofit

**Servidor (API):**
* **Framework & Lenguaje:** FastAPI, Python 3.10+
* **Base de Datos & ORM:** PostgreSQL, SQLAlchemy
* **Seguridad:** Passlib (Bcrypt) & PyJWT
* **Testing:** Pytest

## Arquitectura del Sistema
La plataforma garantiza la escalabilidad delegando responsabilidades. El cliente móvil (Android) actúa exclusivamente como terminal de usuario, sin procesar reglas de negocio críticas. Toda la lógica, la seguridad y las transacciones son orquestadas de forma centralizada por la API.
