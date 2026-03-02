"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Configuración global de pruebas. Establece la base de datos en memoria, usuarios semilla y tokens de autenticación.
"""

import pytest
from sqlalchemy import create_engine
from sqlalchemy.orm import sessionmaker
from sqlalchemy.pool import StaticPool
from fastapi.testclient import TestClient
from main import app, get_db
from database import Base
from functions import crear_access_token
import models

# Configuración de base de datos SQLite en memoria para pruebas rápidas y aisladas
SQLALCHEMY_DATABASE_URL = "sqlite:///:memory:"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL,
    connect_args={"check_same_thread": False},
    poolclass=StaticPool,
)
TestingSessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

# Inicializa la BD, crea tablas y usuarios de prueba (Admin, Terapeuta, Paciente)
@pytest.fixture()
def db():
    Base.metadata.create_all(bind=engine)
    db = TestingSessionLocal()
    
    admin = models.Usuario(
        dni=11111111, 
        correo_electronico="admin@test.com",
        nombre="Admin", apellidos="Test",
        contrasena="contrasena",
        rol=models.RolTipo.admin
    )
    db.add(admin)
    
    terapeuta = models.Usuario(
        dni=22222222, 
        correo_electronico="tera@test.com",
        nombre="Terapeuta", apellidos="Peuta",
        contrasena="contrasena",
        rol=models.RolTipo.terapeuta
    )
    db.add(terapeuta)
    

    paciente = models.Usuario(
        dni=33333333, 
        correo_electronico="paciente@test.com",
        nombre="Paciente", apellidos="Ente",
        contrasena="contrasena",
        rol=models.RolTipo.paciente
    )
    db.add(paciente)
    
    db.commit()

    try:
        yield db
    finally:
        db.close()
        Base.metadata.drop_all(bind=engine)

# Fixture que inyecta la BD de prueba en la app FastAPI y devuelve el cliente HTTP
@pytest.fixture()
def client(db):
    def override_get_db():
        try:
            yield db
        finally:
            db.close()
    
    app.dependency_overrides[get_db] = override_get_db
    yield TestClient(app)
    app.dependency_overrides.clear()

# Genera un token válido de Administrador para los tests
@pytest.fixture()
def token_admin():
    return crear_access_token({"sub": "11111111", "rol": "admin"})

# Genera un token válido de Terapeuta para los tests
@pytest.fixture()
def token_terapeuta():
    return crear_access_token({"sub": "22222222", "rol": "terapeuta"})

# Genera un token válido de Paciente para los tests
@pytest.fixture()
def token_paciente():
    return crear_access_token({"sub": "33333333", "rol": "paciente"})