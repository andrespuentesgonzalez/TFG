"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Definición de los esquemas Pydantic para la validación de datos de entrada y salida de la API.
"""

from pydantic import BaseModel, EmailStr
from typing import Optional, List
from models import RolTipo, ZonaTipo, EstadoSesion 
from datetime import datetime

# =======================
# USUARIOS
# =======================
class UsuarioBase(BaseModel):
    dni: int
    correo_electronico: EmailStr
    nombre: str
    apellidos: str
    rol: RolTipo

class UsuarioCreate(UsuarioBase):
    contrasena: str

class UsuarioResponse(UsuarioBase):
    id_usuario: int
    id_terapeuta_asignado: Optional[int] = None 
    class Config: from_attributes = True

class UsuarioUpdate(BaseModel):
    dni: Optional[int] = None
    correo_electronico: Optional[EmailStr] = None
    nombre: Optional[str] = None
    apellidos: Optional[str] = None
    rol: Optional[RolTipo] = None
    contrasena: Optional[str] = None 
# ----------------------

class AsignarTerapeutaSchema(BaseModel):
    id_terapeuta: int

# =======================
# EJERCICIOS
# =======================
class EjercicioBase(BaseModel):
    nombre: str
    descripcion: str
    duracion: int 
    video_prueba: Optional[str] = None
    zona_afectada: ZonaTipo

class EjercicioCreate(EjercicioBase):
    pass

class EjercicioResponse(EjercicioBase):
    id_ejercicio: int
    id_terapeuta: int 
    terapeuta_rel: Optional[UsuarioResponse] = None
    class Config: from_attributes = True

class EjercicioUpdate(BaseModel):
    nombre: Optional[str] = None
    descripcion: Optional[str] = None
    duracion: Optional[int] = None
    video_prueba: Optional[str] = None
    zona_afectada: Optional[ZonaTipo] = None

# =======================
# FICHAS 
# =======================
class FichaBase(BaseModel):
    nombre: Optional[str] = None
    series: Optional[int] = None
    repeticiones: Optional[int] = None
    tiempo_minutos: Optional[int] = None 

class FichaInput(BaseModel):
    id_ejercicio: int
    series: int = 3
    repeticiones: int = 10
    tiempo_minutos: int = 0

class FichaCreate(FichaBase):
    id_rutina: int
    id_ejercicio: int

class FichaResponse(FichaBase):
    id_ficha_ejercicios: int
    id_rutina: int
    id_ejercicio: int
    class Config: from_attributes = True

class FichaUpdate(BaseModel):
    nombre: Optional[str] = None
    series: Optional[int] = None
    repeticiones: Optional[int] = None
    tiempo_minutos: Optional[int] = None

class FichaConDetalleEjercicio(FichaResponse):
    ejercicio_rel: EjercicioResponse 

# =======================
# RUTINAS
# =======================
class RutinaBase(BaseModel):
    nombre: str
    descripcion: str

class RutinaCreate(RutinaBase):

    ejercicios: List[FichaInput] = [] 

class RutinaResponse(RutinaBase):
    id_rutina: int
    id_terapeuta: int
    terapeuta_rel: Optional[UsuarioResponse] = None
    fichas: List[FichaConDetalleEjercicio] = []

    class Config: from_attributes = True

class RutinaCompleta(RutinaResponse):
    pass

class RutinaUpdate(BaseModel):
    duracion: Optional[int] = None

# =======================
# SESIONES 
# =======================
class SesionBase(BaseModel):
    fecha: datetime
    id_rutina: Optional[int] = None 
    id_paciente: int
    estado: Optional[EstadoSesion] = EstadoSesion.pendiente

class SesionCreate(SesionBase):
    tiempo_preparacion: int = 10

class SesionUpdate(BaseModel):
    fecha: Optional[datetime] = None
    id_rutina: Optional[int] = None
    
    estado: Optional[EstadoSesion] = None 
    tiempo_preparacion: Optional[int] = None

    video_sesion: Optional[str] = None
    comentario_paciente: Optional[str] = None
    puntuacion: Optional[int] = None
    comentario_terapeuta: Optional[str] = None

class SesionEjercicioResponse(BaseModel):
    id: int
    id_ejercicio: int
    video_url: str | None
    
    class ConfigDict:
        from_attributes = True

class SubirVideo(BaseModel):
    video_url: str     

class SesionResponse(SesionBase):
    id_sesion: int
    id_paciente: int
    id_terapeuta: int
    estado: EstadoSesion 
    
    tiempo_preparacion: int 
    ejercicios_detalles: list[SesionEjercicioResponse] = []
    video_sesion: Optional[str] = None
    comentario_paciente: Optional[str] = None
    puntuacion: Optional[int] = None
    comentario_terapeuta: Optional[str] = None
    
    class Config: from_attributes = True

# =======================
# DASHBOARD
# =======================
class LogReciente(BaseModel):
    usuario: str
    accion: str
    tiempo: str
    tipo: str

class AdminStats(BaseModel): 
    totalUsuarios: int
    admins: int
    terapeutas: int
    pacientes: int
    totalEjercicios: int
    totalFichas: int
    totalRutinas: int
    totalSesiones: int
    
    sesionesHoy: int             
    pendientes: int  
    notaMedia: float       
    grafica_semanal: List[int] = []
    logs_recientes: List[LogReciente] = []

    class Config: from_attributes = True

DashboardResponse = AdminStats

# =======================
# ESTADÍSTICAS PACIENTE
# =======================
class PuntoGrafica(BaseModel):
    fecha: datetime
    puntuacion: int

class EstadisticasPacienteResponse(BaseModel):
    promedio_puntuacion: float
    total_sesiones_completadas: int
    maxima_puntuacion: int
    
    evolucion: List[PuntoGrafica] = []

    class Config: from_attributes = True