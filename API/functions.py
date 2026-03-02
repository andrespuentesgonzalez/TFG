"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: Capa de lógica de negocio y funciones CRUD (Create, Read, Update, Delete) que interactúan con la base de datos.
"""

from sqlalchemy.orm import Session
from passlib.context import CryptContext       
from datetime import datetime, timedelta       
from jose import JWTError, jwt                 
from fastapi import HTTPException, status      
from fastapi.security import OAuth2PasswordBearer 
import models, schemas                         

SECRET_KEY = "tu_clave_secreta_super_dificil" 
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = 2628000

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

# ==========================================
# FUNCIONES AUXILIARES DE SEGURIDAD
# ==========================================

# Genera un hash seguro a partir de una contraseña en texto plano utilizando bcrypt
def obtener_hash_contrasena(password):
    return pwd_context.hash(password)

# Compara una contraseña en texto plano con su hash almacenado para verificar la autenticidad
def verificar_contrasena(plain_password, hashed_password):
    return pwd_context.verify(plain_password, hashed_password)

# Crea un token JWT con fecha de expiración para la autenticación de usuarios
def crear_access_token(data: dict):
    to_encode = data.copy()

    expire = datetime.utcnow() + timedelta(minutes=ACCESS_TOKEN_EXPIRE_MINUTES)
    
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
    return encoded_jwt

# Decodifica y valida el token JWT, recuperando el usuario asociado desde la base de datos
def verificar_token(token: str, db: Session):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="No se pudieron validar las credenciales",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, SECRET_KEY, algorithms=[ALGORITHM])
        
        dni_str: str = payload.get("sub")
        if dni_str is None:
            raise credentials_exception
            
        dni_int = int(dni_str)
        
    except (JWTError, ValueError):
        raise credentials_exception
    
    user = obtener_usuario_por_dni(db, dni=dni_int)
    
    if user is None:
        raise credentials_exception
        
    return user

# ==========================================
# LÓGICA DE USUARIOS 
# ==========================================

# Busca un usuario en la base de datos por su dirección de correo electrónico
def obtener_usuario_por_email(db: Session, email: str):
    return db.query(models.Usuario).filter(models.Usuario.correo_electronico == email).first()

# Busca un usuario en la base de datos por su Documento Nacional de Identidad (DNI)
def obtener_usuario_por_dni(db: Session, dni: int):
    return db.query(models.Usuario).filter(models.Usuario.dni == dni).first()

# Busca un usuario en la base de datos por su identificador único (ID)
def obtener_usuario_por_id(db: Session, user_id: int):
    return db.query(models.Usuario).filter(models.Usuario.id_usuario == user_id).first()

# Recupera el listado completo de todos los usuarios registrados en el sistema
def obtener_todos_usuarios(db: Session):
    return db.query(models.Usuario).all()

# Crea un nuevo registro de usuario en la base de datos con la contraseña hasheada
def crear_usuario(db: Session, usuario: schemas.UsuarioCreate):
    hashed_password = obtener_hash_contrasena(usuario.contrasena)
    
    nuevo_usuario = models.Usuario(
        dni=usuario.dni,
        correo_electronico=usuario.correo_electronico,
        nombre=usuario.nombre,
        apellidos=usuario.apellidos,
        contrasena=hashed_password, 
        rol=usuario.rol,
    )
    db.add(nuevo_usuario)
    db.commit()
    db.refresh(nuevo_usuario)
    return nuevo_usuario

# Elimina un usuario existente de la base de datos
def eliminar_usuario(db: Session, db_user: models.Usuario):
    db.delete(db_user)
    db.commit()

# Actualiza los campos modificables de un usuario, incluyendo el cambio de contraseña si se solicita
def actualizar_usuario(db: Session, db_user: models.Usuario, updates: schemas.UsuarioUpdate):
    if updates.dni is not None:
        db_user.dni = updates.dni
        
    if updates.correo_electronico is not None:
        db_user.correo_electronico = updates.correo_electronico
        
    if updates.nombre is not None:
        db_user.nombre = updates.nombre
        
    if updates.apellidos is not None:
        db_user.apellidos = updates.apellidos
    
    if updates.rol is not None:
        db_user.rol = updates.rol

    if updates.contrasena is not None and updates.contrasena.strip() != "":
        db_user.contrasena = obtener_hash_contrasena(updates.contrasena)
    
    try:
        db.commit()
        db.refresh(db_user)
        return db_user
    except Exception as e:
        db.rollback()
        raise e

# Establece la relación entre un paciente y un terapeuta responsable
def asignar_terapeuta(db: Session, paciente: models.Usuario, id_terapeuta: int):
    paciente.id_terapeuta_asignado = id_terapeuta
    db.commit()
    db.refresh(paciente)
    return paciente

# ==========================================
# LÓGICA DE EJERCICIOS 
# ==========================================

# Recupera el listado completo de ejercicios disponibles en la biblioteca
def obtener_todos_ejercicios(db: Session):
    return db.query(models.Ejercicio).all()

# Busca un ejercicio específico por su identificador único
def obtener_ejercicio_por_id(db: Session, id_ejercicio: int):
    return db.query(models.Ejercicio).filter(models.Ejercicio.id_ejercicio == id_ejercicio).first()

# Registra un nuevo ejercicio en el sistema asociado al terapeuta creador
def crear_ejercicio(db: Session, ejercicio: schemas.EjercicioCreate, id_terapeuta_logueado: int):
    nuevo_ejercicio = models.Ejercicio(
        nombre=ejercicio.nombre,
        descripcion=ejercicio.descripcion,
        duracion=ejercicio.duracion,
        video_prueba=ejercicio.video_prueba,
        zona_afectada=ejercicio.zona_afectada,
        id_terapeuta=id_terapeuta_logueado 
    )
    db.add(nuevo_ejercicio)
    db.commit()
    db.refresh(nuevo_ejercicio)
    return nuevo_ejercicio

# Elimina un ejercicio de la base de datos
def eliminar_ejercicio(db: Session, db_ejercicio: models.Ejercicio):
    db.delete(db_ejercicio)
    db.commit()

# Modifica los detalles de un ejercicio existente
def actualizar_ejercicio(db: Session, db_ejercicio: models.Ejercicio, datos: schemas.EjercicioUpdate):
    if datos.nombre is not None:
        db_ejercicio.nombre = datos.nombre
    if datos.descripcion is not None:
        db_ejercicio.descripcion = datos.descripcion
    if datos.duracion is not None:
        db_ejercicio.duracion = datos.duracion
    if datos.video_prueba is not None:
        db_ejercicio.video_prueba = datos.video_prueba
    if datos.zona_afectada is not None:
        db_ejercicio.zona_afectada = datos.zona_afectada

    db.commit()
    db.refresh(db_ejercicio)
    return db_ejercicio

# ==========================================
# LÓGICA DE RUTINAS 
# ==========================================

# Busca una rutina específica por su identificador único
def obtener_rutina_por_id(db: Session, id_rutina: int):
    return db.query(models.Rutina).filter(models.Rutina.id_rutina == id_rutina).first()

# Crea una nueva rutina vacía asignada a un terapeuta
def crear_rutina(db: Session, rutina: schemas.RutinaCreate, id_terapeuta_logueado: int):
    nueva_rutina = models.Rutina(
        nombre=rutina.nombre,
        descripcion=rutina.descripcion,
        id_terapeuta=id_terapeuta_logueado
    )
    db.add(nueva_rutina)
    db.commit()
    db.refresh(nueva_rutina)
    return nueva_rutina

# Elimina una rutina completa del sistema
def eliminar_rutina(db: Session, db_rutina: models.Rutina):
    db.delete(db_rutina)
    db.commit()

# Actualiza la duración estimada u otros metadatos de una rutina
def actualizar_rutina(db: Session, db_rutina: models.Rutina, datos: schemas.RutinaUpdate):
    if datos.duracion is not None:
        db_rutina.duracion = datos.duracion
    
    db.commit()
    db.refresh(db_rutina)
    return db_rutina

# ==========================================
# LÓGICA DE FICHAS 
# ==========================================

# Busca una ficha específica (relación ejercicio-rutina) por su ID
def obtener_ficha_por_id(db: Session, id_ficha: int):
    return db.query(models.FichaEjercicios).filter(models.FichaEjercicios.id_ficha_ejercicios == id_ficha).first()

# Crea una asociación entre un ejercicio y una rutina, especificando repeticiones y tiempo
def crear_ficha_asociativa(db: Session, ficha_data: schemas.FichaCreate):
    nueva_ficha = models.FichaEjercicios(
        nombre=ficha_data.nombre,
        id_rutina=ficha_data.id_rutina,
        id_ejercicio=ficha_data.id_ejercicio,
        tiempo_minutos=ficha_data.tiempo_minutos,
        series=ficha_data.series,
        repeticiones=ficha_data.repeticiones
    )
    db.add(nueva_ficha)
    db.commit()
    db.refresh(nueva_ficha)
    return nueva_ficha

# Elimina un ejercicio de una rutina específica
def eliminar_ficha(db: Session, db_ficha: models.FichaEjercicios):
    db.delete(db_ficha)
    db.commit()

# Modifica los parámetros de realización (series, repeticiones) de un ejercicio dentro de una rutina
def actualizar_ficha(db: Session, db_ficha: models.FichaEjercicios, datos: schemas.FichaUpdate):
    if datos.nombre is not None:
        db_ficha.nombre = datos.nombre
    if datos.tiempo_minutos is not None:
        db_ficha.tiempo_minutos = datos.tiempo_minutos
    if datos.series is not None:
        db_ficha.series = datos.series
    if datos.repeticiones is not None:
        db_ficha.repeticiones = datos.repeticiones
        
    db.commit()
    db.refresh(db_ficha)
    return db_ficha

# ==========================================
# LÓGICA DE SESIONES 
# ==========================================

# Busca una sesión de rehabilitación específica por su ID
def obtener_sesion_por_id(db: Session, id_sesion: int):
    return db.query(models.Sesion).filter(models.Sesion.id_sesion == id_sesion).first()

# Obtiene el listado completo de todas las sesiones registradas en el sistema
def obtener_todas_sesiones(db: Session):
    return db.query(models.Sesion).all()

# Filtra y devuelve todas las sesiones asignadas a un paciente específico
def obtener_sesiones_por_paciente(db: Session, id_paciente: int):
    return db.query(models.Sesion).filter(models.Sesion.id_paciente == id_paciente).all()

# Programa una nueva sesión, vinculando paciente, terapeuta y rutina opcional
def crear_sesion(db: Session, sesion: schemas.SesionCreate, id_terapeuta_logueado: int):
    nueva_sesion = models.Sesion(
        fecha=sesion.fecha,
        id_paciente=sesion.id_paciente, 
        
        id_terapeuta=id_terapeuta_logueado,
        id_rutina=sesion.id_rutina,
    )
    db.add(nueva_sesion)
    db.commit()
    db.refresh(nueva_sesion)
    return nueva_sesion

# Elimina una sesión programada o realizada del historial
def eliminar_sesion(db: Session, db_sesion: models.Sesion):
    db.delete(db_sesion)
    db.commit()

# Actualiza el estado, resultados y retroalimentación de una sesión
def actualizar_sesion(db: Session, db_sesion: models.Sesion, datos: schemas.SesionUpdate):
    if datos.fecha is not None:
        db_sesion.fecha = datos.fecha
    if datos.id_rutina is not None:
        db_sesion.id_rutina = datos.id_rutina
    
    if datos.estado is not None:
        db_sesion.estado = datos.estado

    if datos.video_sesion is not None:
        db_sesion.video_sesion = datos.video_sesion
    if datos.comentario_paciente is not None:
        db_sesion.comentario_paciente = datos.comentario_paciente
        
    if datos.puntuacion is not None:
        db_sesion.puntuacion = datos.puntuacion
    if datos.comentario_terapeuta is not None:
        db_sesion.comentario_terapeuta = datos.comentario_terapeuta

    db.commit()
    db.refresh(db_sesion)
    return db_sesion

# Encuentra la próxima sesión pendiente más cercana en el tiempo para un paciente
def obtener_siguiente_sesion_paciente(db: Session, id_paciente: int):
    ahora = datetime.now()
    return db.query(models.Sesion)\
        .filter(models.Sesion.id_paciente == id_paciente)\
        .filter(models.Sesion.fecha >= ahora)\
        .filter(models.Sesion.estado == models.EstadoSesion.pendiente)\
        .order_by(models.Sesion.fecha.asc())\
        .first()

# Devuelve todas las sesiones creadas o supervisadas por un terapeuta específico
def obtener_sesiones_por_terapeuta(db: Session, id_terapeuta: int):
    return db.query(models.Sesion).filter(models.Sesion.id_terapeuta == id_terapeuta).all()