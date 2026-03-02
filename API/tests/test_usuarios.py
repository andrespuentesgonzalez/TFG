"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Tests de integración para el módulo de Usuarios. Verifica la gestión de permisos, CRUD de usuarios y asignación de terapeutas.
"""

from fastapi import status

usuario_paciente_data = {
    "dni": 99999999,
    "correo_electronico": "paciente.test@hospital.com",
    "nombre": "Pepito",
    "apellidos": "Perez",
    "contrasena": "clave123",
    "rol": "paciente"
}

usuario_terapeuta_data = {
    "dni": 88888888,
    "correo_electronico": "terapeuta.test@hospital.com",
    "nombre": "Laura",
    "apellidos": "Gomez",
    "contrasena": "clave123",
    "rol": "terapeuta"
}

# ==========================================
# 1. TEST LEER USUARIOS 
# ==========================================

# Verifica que un administrador tiene acceso al listado completo de usuarios del sistema
def test_leer_usuarios_como_admin(client, token_admin):
    headers = {"Authorization": f"Bearer {token_admin}"}
    response = client.get("/usuarios/", headers=headers)
    assert response.status_code == status.HTTP_200_OK
    assert isinstance(response.json(), list)

# Comprueba que un terapeuta no tiene permisos para ver el listado global de usuarios
def test_leer_usuarios_como_terapeuta_prohibido(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    response = client.get("/usuarios/", headers=headers)
    assert response.status_code == status.HTTP_403_FORBIDDEN

# ==========================================
# 2. TEST CREAR USUARIOS 
# ==========================================

# Valida que el administrador puede registrar nuevos usuarios correctamente
def test_crear_usuario_como_admin(client, token_admin):
    headers = {"Authorization": f"Bearer {token_admin}"}
    response = client.post("/usuarios/", json=usuario_paciente_data, headers=headers)
    
    assert response.status_code == status.HTTP_200_OK
    data = response.json()
    assert data["dni"] == usuario_paciente_data["dni"]
    assert "id_usuario" in data

# Asegura que el sistema rechaza intentos de registro con DNI o correo ya existentes
def test_crear_usuario_duplicado_falla(client, token_admin):
    headers = {"Authorization": f"Bearer {token_admin}"} 
    client.post("/usuarios/", json=usuario_paciente_data, headers=headers)
    response = client.post("/usuarios/", json=usuario_paciente_data, headers=headers)
    
    assert response.status_code == status.HTTP_400_BAD_REQUEST
    assert "DNI ya registrado" in response.json()["detail"] or "Correo ya registrado" in response.json()["detail"]

# Verifica que la creación de usuarios está restringida exclusivamente a administradores
def test_crear_usuario_como_terapeuta_prohibido(client, token_terapeuta):
    headers = {"Authorization": f"Bearer {token_terapeuta}"}
    response = client.post("/usuarios/", json=usuario_paciente_data, headers=headers)