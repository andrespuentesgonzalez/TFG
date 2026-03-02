"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Definición de los modelos para el mapeo de las tablas de la base de datos.
"""

import enum
from sqlalchemy import Column, Integer, String, ForeignKey, Enum as SQLAlchemyEnum, DateTime
from sqlalchemy.orm import relationship, backref
from database import Base

# --- ENUMS ---
class RolTipo(str, enum.Enum):
    admin = "admin"
    terapeuta = "terapeuta"
    paciente = "paciente"

class ZonaTipo(str, enum.Enum):
    Torso = "Torso"
    Cabeza = "Cabeza"
    Extremidades_Superiores = "Extremidades_Superiores"
    Extremidades_Inferiores = "Extremidades_Inferiores"

class EstadoSesion(str, enum.Enum):
    pendiente = "pendiente"   
    realizada = "realizada"   
    corregida = "corregida"   

# --- TABLAS ---

class Usuario(Base):
    __tablename__ = "usuario"

    id_usuario = Column(Integer, primary_key=True, index=True)
    dni = Column(Integer, unique=True, nullable=False)
    correo_electronico = Column(String(100), unique=True, nullable=False)
    nombre = Column(String(50), nullable=False)
    apellidos = Column(String(50), nullable=False)
    contrasena = Column(String(100), nullable=False)
    rol = Column(SQLAlchemyEnum(RolTipo), nullable=False)

    id_terapeuta_asignado = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=True)

    terapeuta = relationship(
        "Usuario", 
        remote_side=[id_usuario], 
        backref="pacientes" 
    )

    rutinas_creadas = relationship("Rutina", back_populates="terapeuta_rel")


class Rutina(Base):
    __tablename__ = "rutina"

    id_rutina = Column(Integer, primary_key=True, index=True)
    nombre = Column(String(500), nullable = False)
    descripcion = Column(String(500), nullable = False)
    id_terapeuta = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False)

    terapeuta_rel = relationship("Usuario", back_populates="rutinas_creadas")
    fichas = relationship("FichaEjercicios", back_populates="rutina_rel", cascade="all, delete-orphan")


class Ejercicio(Base):
    __tablename__ = "ejercicio"

    id_ejercicio = Column(Integer, primary_key=True, index=True)
    nombre = Column(String(100), nullable=False)
    descripcion = Column(String(255), nullable=False)
    duracion = Column(Integer, nullable=False) 
    video_prueba = Column(String(255), nullable=True) 
    zona_afectada = Column(SQLAlchemyEnum(ZonaTipo), nullable=False)
    
    id_terapeuta = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False)

class FichaEjercicios(Base):
    __tablename__ = "ficha_ejercicios"

    id_ficha_ejercicios = Column(Integer, primary_key=True, index=True)
    
    nombre = Column(String(100), nullable=True)
    
    series = Column(Integer, nullable=True)
    repeticiones = Column(Integer, nullable=True)
    tiempo_minutos = Column(Integer, nullable=True)

    id_rutina = Column(Integer, ForeignKey("rutina.id_rutina"), nullable=False)
    id_ejercicio = Column(Integer, ForeignKey("ejercicio.id_ejercicio"), nullable=False)

    rutina_rel = relationship("Rutina", back_populates="fichas")
    ejercicio_rel = relationship("Ejercicio")


class Sesion(Base):
    __tablename__ = "sesion"

    id_sesion = Column(Integer, primary_key=True, index=True)
    fecha = Column(DateTime, nullable=False)  
    
    id_rutina = Column(Integer, ForeignKey("rutina.id_rutina"), nullable=True)
    id_paciente = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False)
    id_terapeuta = Column(Integer, ForeignKey("usuario.id_usuario"), nullable=False)

    estado = Column(SQLAlchemyEnum(EstadoSesion), default=EstadoSesion.pendiente, nullable=False)

    tiempo_preparacion = Column(Integer, default=10, nullable=False) 
    
    video_sesion = Column(String(255), nullable=True) 
    comentario_paciente = Column(String(255), nullable=True)
    
    puntuacion = Column(Integer, nullable=True) 
    comentario_terapeuta = Column(String(255), nullable=True)
    ejercicios_detalles = relationship("SesionEjercicio", back_populates="sesion", cascade="all, delete-orphan")

    rutina_rel = relationship("Rutina")
    paciente_rel = relationship("Usuario", foreign_keys=[id_paciente])
    terapeuta_rel = relationship("Usuario", foreign_keys=[id_terapeuta])


class SesionEjercicio(Base):
    __tablename__ = "sesion_ejercicios"

    id = Column(Integer, primary_key=True, index=True)
    id_sesion = Column(Integer, ForeignKey("sesion.id_sesion"))
    id_ejercicio = Column(Integer, ForeignKey("ejercicio.id_ejercicio"))
    video_url = Column(String, nullable=True)    
    sesion = relationship("Sesion", back_populates="ejercicios_detalles")
    ejercicio = relationship("Ejercicio")