"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Tests de integración para el módulo de Sesiones. Verifica la planificación, ejecución, evaluación y privacidad de las sesiones.
"""

from fastapi import status
from datetime import datetime, timedelta

fecha_futura = datetime.now() + timedelta(days=1) # Mañana
fecha_pasada = datetime.now() - timedelta(days=1) # Ayer

# ========================================== #
# 1. TEST CREAR SESIÓN                       #
# ========================================== #

# Verifica que un terapeuta puede programar una nueva sesión de rehabilitación para un paciente
def test_crear_sesion_terapeuta(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    
    sesion_data = {
        "fecha": fecha_futura.isoformat(),
        "id_paciente": 3, 
        "id_rutina": None 
    }
    
    response = client.post("/sesiones/", json=sesion_data, headers=headers)
    
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["id_paciente"] == 3
    assert data["id_terapeuta"] == 2 
    assert data["puntuacion"] is None 

# ========================================== #
# 2. TEST FLUJO DE EVALUACIÓN                #
# ========================================== #

# Valida el ciclo completo: programación, realización por el paciente (subida de vídeo) y evaluación por el terapeuta
def test_flujo_completo_evaluacion(client, token_terapeuta, token_paciente):
    headers_tera = {"Authorization": f"Bearer {token_terapeuta}"}
    headers_paci = {"Authorization": f"Bearer {token_paciente}"}
    
    sesion_data = {"fecha": fecha_futura.isoformat(), "id_paciente": 3}
    res_create = client.post("/sesiones/", json=sesion_data, headers=headers_tera)
    id_sesion = res_create.json()["id_sesion"]
    
    datos_video = {
        "video_sesion": "http://video_paciente.mp4",
        "comentario_paciente": "Me costó un poco"
    }
    res_video = client.put(f"/sesiones/{id_sesion}", json=datos_video, headers=headers_paci)
    assert res_video.status_code == status.HTTP_200_OK
    assert res_video.json()["video_sesion"] == "http://video_paciente.mp4"

    datos_correccion = {
        "puntuacion": 8,
        "comentario_terapeuta": "Muy bien ejecutado"
    }
    res_nota = client.put(f"/sesiones/{id_sesion}", json=datos_correccion, headers=headers_tera)
    assert res_nota.status_code == status.HTTP_200_OK
    assert res_nota.json()["puntuacion"] == 8

# ========================================== #
# 3. TEST VISIBILIDAD                        #
# ========================================== #

# Asegura que los pacientes pueden consultar su propio historial pero no acceder a IDs arbitrarios o prohibidos
def test_ver_mis_sesiones(client, token_paciente, token_terapeuta):
    headers_paci = {"Authorization": f"Bearer {token_paciente}"}
    headers_tera = {"Authorization": f"Bearer {token_terapeuta}"}
    
    sesion_data = {"fecha": fecha_futura.isoformat(), "id_paciente": 3}
    client.post("/sesiones/", json=sesion_data, headers=headers_tera)

    res_paci = client.get("/usuarios/3/sesiones", headers=headers_paci)
    assert res_paci.status_code == status.HTTP_200_OK
    assert len(res_paci.json()) >= 1
    
    res_fail = client.get("/usuarios/999/sesiones", headers=headers_paci)
    assert res_fail.status_code == status.HTTP_403_FORBIDDEN

# =========================================== #
# 4. TEST PRÓXIMA SESIÓN (Dashboard Paciente) #
# =========================================== #

# Verifica la recuperación de la sesión pendiente más próxima para mostrarla en el panel principal del paciente
def test_siguiente_sesion(client, token_terapeuta, token_paciente):
    headers_tera = {"Authorization": f"Bearer {token_terapeuta}"}
    headers_paci = {"Authorization": f"Bearer {token_paciente}"}
    
    s_futura = {"fecha": fecha_futura.isoformat(), "id_paciente": 3}
    client.post("/sesiones/", json=s_futura, headers=headers_tera)
    
    res = client.get("/usuarios/3/siguiente-sesion", headers=headers_paci)
    assert res.status_code == status.HTTP_200_OK

    fecha_devuelta = res.json()["fecha"]
    assert fecha_devuelta.startswith(fecha_futura.isoformat()[:10])

# ========================================== #
# 5. TEST DE PRIVACIDAD                      #
# ========================================== #

# Comprobación estricta de privacidad: un paciente no puede acceder a los datos de sesión de otro paciente
def test_paciente_no_ve_sesion_ajena(client, token_terapeuta, token_paciente):
    headers_tera = {"Authorization": f"Bearer {token_terapeuta}"}
    headers_paci = {"Authorization": f"Bearer {token_paciente}"}
    
    client.post("/sesiones/", json={"fecha": fecha_futura.isoformat(), "id_paciente": 3}, headers=headers_tera)
    
    client.post("/sesiones/", json={"fecha": fecha_futura.isoformat(), "id_paciente": 2}, headers=headers_tera)
    
    res = client.get("/usuarios/2/sesiones", headers=headers_paci)
    
    assert res.status_code == status.HTTP_403_FORBIDDEN
    assert res.json()["detail"] == "No tienes permiso para ver las sesiones de otro paciente"