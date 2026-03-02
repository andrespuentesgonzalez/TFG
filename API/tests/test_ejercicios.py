"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Tests de integración para el módulo de Ejercicios. Verifica la creación, listado, eliminación y validación de datos.
"""

from fastapi import status

# Datos de prueba
ejercicio_data = {
    "nombre": "Levantamiento de brazos",
    "descripcion": "Subir los brazos lateralmente",
    "duracion": 15,
    "video_prueba": "http://video.com/ejemplo",
    "zona_afectada": "Extremidades_Superiores"
}

# ========================================== #
# 1. TEST CREAR EJERCICIO                    #
# ========================================== #

# Verifica que un terapeuta autenticado puede crear un nuevo ejercicio correctamente
def test_crear_ejercicio_como_terapeuta(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    
    response = client.post("/ejercicios/", json=ejercicio_data, headers=headers)
    
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["nombre"] == ejercicio_data["nombre"]
    assert "id_ejercicio" in data
    assert data["id_terapeuta"] == 2 

# Comprueba que un paciente no tiene permisos para crear ejercicios (debe devolver 403)
def test_crear_ejercicio_como_paciente_prohibido(client, token_paciente):
    headers = {"Authorization": f"Bearer {token_paciente}"}
    
    response = client.post("/ejercicios/", json=ejercicio_data, headers=headers)
    
    assert response.status_code == status.HTTP_403_FORBIDDEN

# ========================================== #
# 2. TEST LISTAR EJERCICIOS                  #
# ========================================== #

# Asegura que los terapeutas y administradores pueden obtener el listado de ejercicios
def test_listar_ejercicios(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    
    client.post("/ejercicios/", json=ejercicio_data, headers=headers)
    
    response = client.get("/ejercicios/", headers=headers)
    
    assert response.status_code == status.HTTP_200_OK
    assert isinstance(response.json(), list)
    assert len(response.json()) >= 1

# ========================================== #
# 3. TEST BORRAR EJERCICIO                   #
# ========================================== #

# Valida el flujo de creación y eliminación de un ejercicio, asegurando que ya no existe tras el borrado
def test_borrar_ejercicio_propio(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    
    res_crear = client.post("/ejercicios/", json=ejercicio_data, headers=headers)
    id_ejercicio = res_crear.json()["id_ejercicio"]

    res_borrar = client.delete(f"/ejercicios/{id_ejercicio}", headers=headers)
    assert res_borrar.status_code == status.HTTP_200_OK
    
    res_check = client.delete(f"/ejercicios/{id_ejercicio}", headers=headers)
    assert res_check.status_code == status.HTTP_404_NOT_FOUND

# Verifica que la API rechaza datos incorrectos (enum inválido o campos faltantes) con un error 422
def test_crear_ejercicio_datos_invalidos(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    data_bad_enum = {
        "nombre": "Mal Ejercicio",
        "descripcion": "desc",
        "duracion": 10,
        "zona_afectada": "Ojo_Derecho" 
    }
    res1 = client.post("/ejercicios/", json=data_bad_enum, headers=headers)
    assert res1.status_code == 422 
    
    data_missing = {
        "nombre": "Sin duración"
    }
    res2 = client.post("/ejercicios/", json=data_missing, headers=headers)
    assert res2.status_code == 422