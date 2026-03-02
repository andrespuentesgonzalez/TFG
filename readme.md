#  HERRAMIENTA DE GESTION PARA EL SEGUIMIENTO DE PACIENTES CON VIDEO A TRAVES DE DISPOSITIVO MOVIL

![Python](https://img.shields.io/badge/Python-3.10+-blue.svg)
![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688.svg?logo=fastapi)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-336791.svg?logo=postgresql)
![License](https://img.shields.io/badge/License-MIT-green.svg)

Backend robusto desarrollado con **FastAPI** y **PostgreSQL** para dar soporte a una plataforma móvil de telerrehabilitación asíncrona. 

Este proyecto fue desarrollado como Trabajo de Fin de Grado (TFG) para facilitar el seguimiento de pacientes con enfermedades neurodegenerativas (como el Parkinson), permitiendo a los terapeutas prescribir rutinas, y a los pacientes ejecutar y grabar su terapia desde casa con una interfaz de "mínima interacción".

##  Características Principales

El sistema está diseñado bajo una arquitectura RESTful, priorizando la seguridad de los datos médicos y el desacoplamiento entre cliente y servidor.

* **Autenticación y Autorización (JWT):** Sistema de control de acceso basado en roles estrictos (RBAC). 
    * `Administrador`: Gestión de altas y vinculación clínica Paciente-Terapeuta.
    * `Terapeuta`: Creación de catálogo de ejercicios, rutinas personalizadas y evaluación diferida.
    * `Paciente`: Endpoint de solo lectura para agendas y subida de evidencias en vídeo.
* **Gestión Multimedia:** Recepción, almacenamiento en disco y servicio estático de archivos `.mp4` pesados generados durante la ejecución de las terapias.
* **Integridad Referencial Fuerte:** Esquema de base de datos normalizado que evita anomalías (ej: borrado en cascada controlado para mantener el historial clínico intacto aunque cambie el terapeuta asignado).
* **Procesamiento de Estadísticas:** Endpoints dedicados para agregar datos en bruto y devolver métricas calculadas (notas medias, progresión) al cliente móvil, optimizando su consumo de batería.

##  Stack Tecnológico

* **Framework:** FastAPI
* **Lenguaje:** Python 3.10+
* **Base de Datos:** PostgreSQL
* **ORM:** SQLAlchemy
* **Validación de Datos:** Pydantic
* **Seguridad:** Passlib (Bcrypt) & PyJWT
* **Testing:** Pytest



