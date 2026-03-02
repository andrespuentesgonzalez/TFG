"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Tests de integración para el módulo de Fichas. Verifica la correcta asociación entre Ejercicios y Rutinas.
"""

from fastapi import status

rutina_data = {
    "nombre": "Rutina de Fuerza",
    "descripcion": "Ejercicios intensos"
}

ejercicio_data = {
    "nombre": "Sentadilla Silla",
    "descripcion": "Levantarse y sentarse",
    "duracion": 10,
    "video_prueba": "http://video.com",
    "zona_afectada": "Extremidades_Inferiores"
}

# Verifica el flujo completo: crear ejercicio, crear rutina, vincularlos mediante una ficha y comprobar que la rutina devuelve los datos anidados correctamente
def test_crear_ficha_y_verificar_union(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    
    res_ej = client.post("/ejercicios/", json=ejercicio_data, headers=headers)
    assert res_ej.status_code == 200
    id_ejercicio = res_ej.json()["id_ejercicio"]
    
    res_rut = client.post("/rutinas/", json=rutina_data, headers=headers)
    assert res_rut.status_code == 200
    id_rutina = res_rut.json()["id_rutina"]
    
    ficha_data = {
        "nombre": "Serie Principal",
        "id_rutina": id_rutina,
        "id_ejercicio": id_ejercicio
    }
    res_ficha = client.post("/fichas/", json=ficha_data, headers=headers)
    assert res_ficha.status_code == status.HTTP_200_OK
    
    res_get = client.get(f"/rutinas/{id_rutina}", headers=headers)
    assert res_get.status_code == status.HTTP_200_OK
    
    datos_rutina = res_get.json()
    
    assert len(datos_rutina["fichas"]) > 0
    
    ficha_recuperada = datos_rutina["fichas"][0]
    assert ficha_recuperada["id_ejercicio"] == id_ejercicio
    
    assert ficha_recuperada["ejercicio_rel"]["nombre"] == "Sentadilla Silla"
    assert ficha_recuperada["ejercicio_rel"]["zona_afectada"] == "Extremidades_Inferiores"