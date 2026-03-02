"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Tests de integración para el módulo de Rutinas. Verifica permisos y gestión del ciclo de vida de las rutinas.
"""

from fastapi import status

# DATOS DE PRUEBA
rutina_data = {
    "nombre": "Rutina Matutina",
    "descripcion": "Para activar articulaciones"
}

# Verifica que un terapeuta puede crear una nueva rutina asignada a su perfil
def test_crear_rutina(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    
    response = client.post("/rutinas/", json=rutina_data, headers=headers)
    
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["nombre"] == rutina_data["nombre"]
    assert "id_rutina" in data
    assert data["id_terapeuta"] == 2

# Comprueba que los pacientes no tienen permisos para crear rutinas
def test_paciente_no_puede_crear_rutinas(client, token_paciente):
    headers = {"Authorization": f"Bearer {token_paciente}"}
    response = client.post("/rutinas/", json=rutina_data, headers=headers)
    assert response.status_code == status.HTTP_403_FORBIDDEN

# Valida el ciclo de vida de una rutina (creación y eliminación) asegurando su borrado
def test_borrar_rutina(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    
    res = client.post("/rutinas/", json=rutina_data, headers=headers)
    id_rutina = res.json()["id_rutina"]
    
    client.delete(f"/rutinas/{id_rutina}", headers=headers)
    
    res_check = client.get(f"/rutinas/{id_rutina}", headers=headers)
    assert res_check.status_code == status.HTTP_404_NOT_FOUND