"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Tests de integración para verificar la corrección de los cálculos estadísticos y la generación de reportes.
"""

from fastapi import status
from datetime import datetime
import pytest

# Comprueba que el sistema calcula correctamente la media y los totales ignorando las sesiones pendientes
def test_estadisticas_paciente_calculo_media(client, token_terapeuta, token_paciente):
    headers_tera = {"Authorization": f"Bearer {token_terapeuta}"}
    headers_paci = {"Authorization": f"Bearer {token_paciente}"}
    
    s1 = client.post("/sesiones/", json={"fecha": datetime.now().isoformat(), "id_paciente": 3}, headers=headers_tera).json()
    client.put(f"/sesiones/{s1['id_sesion']}", json={"puntuacion": 10}, headers=headers_tera)
    
    s2 = client.post("/sesiones/", json={"fecha": datetime.now().isoformat(), "id_paciente": 3}, headers=headers_tera).json()
    client.put(f"/sesiones/{s2['id_sesion']}", json={"puntuacion": 5}, headers=headers_tera)
    
    client.post("/sesiones/", json={"fecha": datetime.now().isoformat(), "id_paciente": 3}, headers=headers_tera)

    res = client.get("/usuarios/3/estadisticas", headers=headers_paci)
    assert res.status_code == 200
    stats = res.json()
    
    assert stats["total_sesiones_completadas"] == 2
    assert stats["promedio_puntuacion"] == 7.5
    assert stats["maxima_puntuacion"] == 10