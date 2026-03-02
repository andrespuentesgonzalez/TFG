"""
Proyecto: GII 24.36 Herramienta de gestión para el seguimiento de pacientes con vídeo a través de dispositivo móvil.
Autor: Andres Puentes Gonzalez
Descripción: API Principal (FastAPI). Gestiona las rutas, autenticación, lógica de negocio y comunicación con la base de datos.
"""

from fastapi import FastAPI, Depends, HTTPException, status
from sqlalchemy.orm import Session
from fastapi.security import OAuth2PasswordRequestForm
import models, schemas, functions
from database import engine, SessionLocal
from sqlalchemy import func, desc
from datetime import date, timedelta

models.Base.metadata.create_all(bind=engine)

app = FastAPI()

# Dependencia para obtener la sesión de base de datos
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# ==========================================
# 1. LOGIN Y SEGURIDAD
# ==========================================

# Autentica al usuario mediante DNI y contraseña, devolviendo un token de acceso
@app.post("/token")
def login_para_access_token(form_data: OAuth2PasswordRequestForm = Depends(), db: Session = Depends(get_db)):
    try:
        dni_login = int(form_data.username)
    except ValueError:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="El usuario debe ser el DNI (números sin letra)",
            headers={"WWW-Authenticate": "Bearer"},
        )

    user = functions.obtener_usuario_por_dni(db, dni=dni_login)
    
    if not user or not functions.verificar_contrasena(form_data.password, user.contrasena):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="DNI o contraseña incorrectos",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token = functions.crear_access_token(
        data={"sub": str(user.dni), "rol": user.rol.value}
    )
    return {
        "access_token": access_token, 
        "token_type": "bearer",
        "rol": user.rol,
        "usuario": user
    }

def obtener_usuario_actual(token: str = Depends(functions.oauth2_scheme), db: Session = Depends(get_db)):
    return functions.verificar_token(token, db)

def solo_terapeutas(current_user: models.Usuario = Depends(obtener_usuario_actual)):
    if current_user.rol != models.RolTipo.terapeuta:
        raise HTTPException(status_code=403, detail="Permisos insuficientes. Solo terapeutas.")
    return current_user

def solo_admins(current_user: models.Usuario = Depends(obtener_usuario_actual)):
    if current_user.rol != models.RolTipo.admin:
        raise HTTPException(status_code=403, detail="Acceso solo para administradores")
    return current_user

def admin_o_terapeuta(current_user: models.Usuario = Depends(obtener_usuario_actual)):
    if current_user.rol not in [models.RolTipo.admin, models.RolTipo.terapeuta]:
        raise HTTPException(status_code=403, detail="Acceso restringido a Admin o Terapeuta")
    return current_user

# ========================================== 
#               USUARIOS                     
# ========================================== 

# Registra un nuevo usuario en la base de datos (Admin)
@app.post("/usuarios", response_model=schemas.UsuarioResponse)
def crear_usuario(
    usuario: schemas.UsuarioCreate, 
    db: Session = Depends(get_db),
    usuario_actual: models.Usuario = Depends(solo_admins)
):
    if functions.obtener_usuario_por_email(db, usuario.correo_electronico):
        raise HTTPException(status_code=400, detail="Correo ya registrado")
    if functions.obtener_usuario_por_dni(db, usuario.dni):
        raise HTTPException(status_code=400, detail="DNI ya registrado")

    return functions.crear_usuario(db, usuario)

# Obtiene el listado completo de usuarios registrados
@app.get("/usuarios", response_model=list[schemas.UsuarioResponse])
def leer_usuarios(db: Session = Depends(get_db), usuario_actual=Depends(admin_o_terapeuta)):
    return functions.obtener_todos_usuarios(db)

# Lista todos los pacientes asignados al terapeuta logueado
@app.get("/usuarios/mis-pacientes", response_model=list[schemas.UsuarioResponse])
def leer_mis_pacientes(db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    pacientes = db.query(models.Usuario).filter(
        models.Usuario.rol == models.RolTipo.paciente,
        models.Usuario.id_terapeuta_asignado == usuario_actual.id_usuario
    ).all()
    return pacientes

# Obtiene un usuario específico por su ID
@app.get("/usuarios/{id_usuario}", response_model=schemas.UsuarioResponse)
def leer_usuario_individual(
    id_usuario: int, 
    db: Session = Depends(get_db), 
    usuario_actual=Depends(admin_o_terapeuta)
):
    usuario = functions.obtener_usuario_por_id(db, id_usuario)
    if usuario is None:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return usuario

# Elimina un usuario del sistema por su identificador
@app.delete("/usuarios/{id_usuario}")
def borrar_usuario(id_usuario: int, db: Session = Depends(get_db), usuario_actual=Depends(solo_admins)):
    db_user = functions.obtener_usuario_por_id(db, id_usuario)
    if db_user is None:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    functions.eliminar_usuario(db, db_user)
    return {"mensaje": f"El usuario con ID {id_usuario} ha sido eliminado correctamente"}

# Modifica los datos de un usuario existente
@app.put("/usuarios/{id_usuario}", response_model=schemas.UsuarioResponse)
def actualizar_usuario(
    id_usuario: int, 
    usuario_actualizado: schemas.UsuarioUpdate,  # <--- CAMBIO AQUÍ: Usamos el esquema opcional
    db: Session = Depends(get_db), 
    usuario_actual=Depends(solo_admins)
):
    db_user = functions.obtener_usuario_por_id(db, id_usuario)
    if db_user is None:
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    return functions.actualizar_usuario(db, db_user, usuario_actualizado)

# Asigna un terapeuta responsable a un paciente específico
@app.patch("/usuarios/{id_paciente}/asignar-terapeuta", response_model=schemas.UsuarioResponse)
def asignar_paciente_a_terapeuta(id_paciente: int, asignacion: schemas.AsignarTerapeutaSchema, db: Session = Depends(get_db), usuario_actual=Depends(solo_admins)):
    paciente = functions.obtener_usuario_por_id(db, id_paciente)
    if not paciente or paciente.rol != models.RolTipo.paciente:
        raise HTTPException(status_code=404, detail="Paciente no encontrado o no es un paciente")
    
    terapeuta = functions.obtener_usuario_por_id(db, asignacion.id_terapeuta)
    if not terapeuta or terapeuta.rol != models.RolTipo.terapeuta:
        raise HTTPException(status_code=404, detail="Terapeuta no encontrado o no es terapeuta")

    return functions.asignar_terapeuta(db, paciente, terapeuta.id_usuario)

# Devuelve el historial de sesiones de rehabilitación de un paciente
@app.get("/usuarios/{id_paciente}/sesiones", response_model=list[schemas.SesionResponse])
def ver_sesiones_paciente(
    id_paciente: int, 
    db: Session = Depends(get_db), 
    usuario_actual=Depends(obtener_usuario_actual)
):
    if usuario_actual.rol == models.RolTipo.paciente:
        if usuario_actual.id_usuario != id_paciente:
            raise HTTPException(
                status_code=status.HTTP_403_FORBIDDEN, 
                detail="No tienes permiso para ver las sesiones de otro paciente"
            )
    
    if not functions.obtener_usuario_por_id(db, id_paciente):
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
        
    return functions.obtener_sesiones_por_paciente(db, id_paciente)

# Obtiene la información del terapeuta asignado a un paciente
@app.get("/usuarios/{id_paciente}/terapeuta", response_model=schemas.UsuarioResponse)
def leer_terapeuta_asignado(id_paciente: int, db: Session = Depends(get_db), usuario_actual=Depends(obtener_usuario_actual)):
    usuario_paciente = functions.obtener_usuario_por_id(db, id_paciente)
    if not usuario_paciente:
        raise HTTPException(status_code=404, detail="Paciente no encontrado")
    if usuario_paciente.id_terapeuta_asignado is None:
        raise HTTPException(status_code=404, detail="Este paciente no tiene terapeuta asignado")
    return usuario_paciente.terapeuta

# Recupera la próxima sesión pendiente de realizar de un paciente
@app.get("/usuarios/{id_paciente}/siguiente-sesion", response_model=schemas.SesionResponse)
def ver_siguiente_sesion(
    id_paciente: int, 
    db: Session = Depends(get_db), 
    usuario_actual=Depends(obtener_usuario_actual)
):
    if not functions.obtener_usuario_por_id(db, id_paciente):
        raise HTTPException(status_code=404, detail="Usuario no encontrado")
    siguiente = db.query(models.Sesion).filter(
        models.Sesion.id_paciente == id_paciente,
        models.Sesion.estado == models.EstadoSesion.pendiente 
    ).order_by(models.Sesion.fecha.asc()).first()
    
    if not siguiente:
        raise HTTPException(status_code=404, detail="No hay próximas sesiones pendientes")
        
    return siguiente

# Calcula estadísticas de desempeño y evolución de un paciente
@app.get("/usuarios/{id_paciente}/estadisticas", response_model=schemas.EstadisticasPacienteResponse)
def obtener_estadisticas_paciente(
    id_paciente: int, 
    db: Session = Depends(get_db), 
    usuario_actual=Depends(obtener_usuario_actual)
):
    paciente = functions.obtener_usuario_por_id(db, id_paciente)
    if not paciente:
        raise HTTPException(status_code=404, detail="Paciente no encontrado")

    sesiones_evaluadas = db.query(models.Sesion).filter(
        models.Sesion.id_paciente == id_paciente,
        models.Sesion.puntuacion != None
    ).order_by(models.Sesion.fecha.asc()).all()

    total = len(sesiones_evaluadas)
    
    if total == 0:
        return schemas.EstadisticasPacienteResponse(
            promedio_puntuacion=0.0,
            total_sesiones_completadas=0,
            maxima_puntuacion=0,
            evolucion=[]
        )


    lista_notas = [s.puntuacion for s in sesiones_evaluadas]
    promedio = sum(lista_notas) / total
    maxima = max(lista_notas)


    puntos_grafica = []
    for s in sesiones_evaluadas:
        puntos_grafica.append(schemas.PuntoGrafica(
            fecha=s.fecha,
            puntuacion=s.puntuacion
        ))

    return schemas.EstadisticasPacienteResponse(
        promedio_puntuacion=round(promedio, 1),
        total_sesiones_completadas=total,
        maxima_puntuacion=maxima,
        evolucion=puntos_grafica
    )



# ========================================== 
#               EJERCICIOS                   
# ========================================== 

# Crea un nuevo ejercicio en la biblioteca
@app.post("/ejercicios", response_model=schemas.EjercicioResponse)
def crear_ejercicio(
    ejercicio: schemas.EjercicioCreate, 
    db: Session = Depends(get_db),
    usuario_actual: models.Usuario = Depends(solo_terapeutas)
):
    return functions.crear_ejercicio(db, ejercicio, id_terapeuta_logueado=usuario_actual.id_usuario)

# Devuelve el listado de todos los ejercicios disponibles en el sistema
@app.get("/ejercicios", response_model=list[schemas.EjercicioResponse])
def listar_todos_ejercicios(db: Session = Depends(get_db), usuario_actual=Depends(admin_o_terapeuta)):
    return db.query(models.Ejercicio).all()

# Elimina un ejercicio específico de la base de datos
@app.delete("/ejercicios/{id_ejercicio}")
def borrar_ejercicio(id_ejercicio: int, db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    ejercicio = functions.obtener_ejercicio_por_id(db, id_ejercicio)
    if not ejercicio:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado")
    functions.eliminar_ejercicio(db, ejercicio)
    return {"mensaje": f"El ejercicio con ID {id_ejercicio} ha sido eliminado"}

# Actualiza la información de un ejercicio existente
@app.put("/ejercicios/{id_ejercicio}", response_model=schemas.EjercicioResponse)
def actualizar_ejercicio(id_ejercicio: int, datos: schemas.EjercicioUpdate, db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    ejercicio = functions.obtener_ejercicio_por_id(db, id_ejercicio)
    if not ejercicio:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado")
    return functions.actualizar_ejercicio(db, ejercicio, datos)

# ========================================== 
#             FICHAS (ITEMS RUTINA)           
# ========================================== 

# Añade un ejercicio a una rutina existente (crea una ficha)
@app.post("/fichas", response_model=schemas.FichaResponse)
def crear_ficha(
    ficha: schemas.FichaCreate, 
    db: Session = Depends(get_db), 
    usuario_actual=Depends(solo_terapeutas)
):
    rutina = db.query(models.Rutina).filter(models.Rutina.id_rutina == ficha.id_rutina).first()
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")

    ejercicio = db.query(models.Ejercicio).filter(models.Ejercicio.id_ejercicio == ficha.id_ejercicio).first()
    if not ejercicio:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado")

    series_val = ficha.series if (ficha.series is not None and ficha.series > 0) else 3
    repes_val = ficha.repeticiones if (ficha.repeticiones is not None and ficha.repeticiones > 0) else 10
    tiempo_val = ficha.tiempo_minutos if (ficha.tiempo_minutos is not None and ficha.tiempo_minutos > 0) else 0

    nueva_ficha = models.FichaEjercicios(
        id_rutina=ficha.id_rutina,
        id_ejercicio=ficha.id_ejercicio,
        series=series_val,
        repeticiones=repes_val,
        tiempo_minutos=tiempo_val,
        nombre=ejercicio.nombre 
    )

    try:
        db.add(nueva_ficha)
        db.commit()
        db.refresh(nueva_ficha)
        return nueva_ficha
    except Exception as e:
        db.rollback()
        print(f"ERROR DB AL CREAR FICHA: {e}") 
        raise HTTPException(status_code=500, detail=f"Error al guardar ficha: {str(e)}")

# Obtiene los detalles de una ficha de ejercicio específica
@app.get("/fichas/{id_ficha}", response_model=schemas.FichaConDetalleEjercicio)
def ver_ficha_detalle(id_ficha: int, db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    ficha = functions.obtener_ficha_por_id(db, id_ficha)
    if not ficha:
        raise HTTPException(status_code=404, detail="Ficha no encontrada")
    return ficha

# Elimina una ficha (ejercicio) de una rutina
@app.delete("/fichas/{id_ficha}")
def borrar_ficha(id_ficha: int, db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    ficha = functions.obtener_ficha_por_id(db, id_ficha)
    if not ficha:
        raise HTTPException(status_code=404, detail="Ficha no encontrada")
    functions.eliminar_ficha(db, ficha)
    return {"mensaje": f"La ficha con ID {id_ficha} ha sido eliminada"}

# ========================================== 
#                 RUTINAS                    
# ========================================== 

# Crea una nueva rutina de ejercicios asignada al terapeuta
@app.post("/rutinas", response_model=schemas.RutinaResponse)
def crear_rutina(
    rutina_data: schemas.RutinaCreate, 
    db: Session = Depends(get_db), 
    usuario_actual: models.Usuario = Depends(solo_terapeutas)
):
    nueva_rutina = models.Rutina(
        nombre=rutina_data.nombre,
        descripcion=rutina_data.descripcion,
        id_terapeuta=usuario_actual.id_usuario
    )
    
    db.add(nueva_rutina)
    db.flush() 
    
    for item in rutina_data.ejercicios:
        ejercicio_db = db.query(models.Ejercicio).filter(models.Ejercicio.id_ejercicio == item.id_ejercicio).first()
        
        if ejercicio_db:
            nueva_ficha = models.FichaEjercicios(
                id_rutina=nueva_rutina.id_rutina,
                id_ejercicio=item.id_ejercicio,
                series=item.series,
                repeticiones=item.repeticiones,
                tiempo_minutos=item.tiempo_minutos,
                nombre=ejercicio_db.nombre
            )
            db.add(nueva_ficha)
    
    db.commit()
    db.refresh(nueva_rutina)
    
    return nueva_rutina

# Lista todas las rutinas registradas en el sistema
@app.get("/rutinas", response_model=list[schemas.RutinaResponse])
def leer_rutinas(db: Session = Depends(get_db), usuario_actual=Depends(admin_o_terapeuta)):
    return db.query(models.Rutina).all()

# Obtiene la información completa de una rutina incluyendo sus ejercicios
@app.get("/rutinas/{id_rutina}", response_model=schemas.RutinaCompleta)
def ver_rutina_completa(id_rutina: int, db: Session = Depends(get_db), usuario_actual=Depends(obtener_usuario_actual)):
    rutina = functions.obtener_rutina_por_id(db, id_rutina)
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")
    return rutina

# Elimina una rutina completa del sistema
@app.delete("/rutinas/{id_rutina}")
def borrar_rutina(id_rutina: int, db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    rutina = functions.obtener_rutina_por_id(db, id_rutina)
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")
    functions.eliminar_rutina(db, rutina)
    return {"mensaje": f"La rutina con ID {id_rutina} ha sido eliminada"}

# Modifica los datos básicos de una rutina
@app.put("/rutinas/{id_rutina}", response_model=schemas.RutinaResponse)
def actualizar_rutina(id_rutina: int, datos: schemas.RutinaUpdate, db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    rutina = functions.obtener_rutina_por_id(db, id_rutina)
    if not rutina:
        raise HTTPException(status_code=404, detail="Rutina no encontrada")
    return functions.actualizar_rutina(db, rutina, datos)

# ========================================== 
#                 SESIONES                   
# ========================================== 

# Programa una nueva sesión de rehabilitación para un paciente
@app.post("/sesiones", response_model=schemas.SesionResponse)
def crear_sesion(sesion: schemas.SesionCreate, db: Session = Depends(get_db), usuario_actual: models.Usuario = Depends(solo_terapeutas)):
    nueva_sesion = models.Sesion(
        fecha=sesion.fecha,
        id_paciente=sesion.id_paciente,
        id_terapeuta=usuario_actual.id_usuario,
        id_rutina=sesion.id_rutina,
        estado=models.EstadoSesion.pendiente,
        
        tiempo_preparacion=sesion.tiempo_preparacion 
    )
    
    db.add(nueva_sesion)
    db.commit()
    db.refresh(nueva_sesion)
    
    if sesion.id_rutina:
        fichas = db.query(models.FichaEjercicios).filter(models.FichaEjercicios.id_rutina == sesion.id_rutina).all()
        for ficha in fichas:
            nuevo_hueco = models.SesionEjercicio(
                id_sesion=nueva_sesion.id_sesion,
                id_ejercicio=ficha.id_ejercicio,
                video_url=None 
            )
            db.add(nuevo_hueco)
        db.commit()
        db.refresh(nueva_sesion)
        
    return nueva_sesion

# Lista todas las sesiones, filtrando si el solicitante es paciente
@app.get("/sesiones", response_model=list[schemas.SesionResponse])
def listar_sesiones(db: Session = Depends(get_db), usuario_actual=Depends(obtener_usuario_actual)):
    if usuario_actual.rol == models.RolTipo.paciente:
        return functions.obtener_sesiones_por_paciente(db, usuario_actual.id_usuario)
    
    return functions.obtener_todas_sesiones(db)

# Devuelve las sesiones asociadas al usuario actual (paciente o terapeuta)
@app.get("/sesiones/mias", response_model=list[schemas.SesionResponse])
def ver_mis_sesiones(
    db: Session = Depends(get_db), 
    usuario_actual=Depends(obtener_usuario_actual)
):
    if usuario_actual.rol == models.RolTipo.terapeuta:
        return functions.obtener_sesiones_por_terapeuta(db, usuario_actual.id_usuario)
    elif usuario_actual.rol == models.RolTipo.paciente:
        return functions.obtener_sesiones_por_paciente(db, usuario_actual.id_usuario)
    return []

# Obtiene los detalles de una sesión específica por su ID
@app.get("/sesiones/{id_sesion}", response_model=schemas.SesionResponse)
def leer_sesion_por_id(id_sesion: int, db: Session = Depends(get_db), usuario_actual=Depends(obtener_usuario_actual)):
    sesion = functions.obtener_sesion_por_id(db, id_sesion)
    if not sesion:
        raise HTTPException(status_code=404, detail="Sesión no encontrada")
    return sesion

# Elimina una sesión programada del sistema
@app.delete("/sesiones/{id_sesion}")
def borrar_sesion(id_sesion: int, db: Session = Depends(get_db), usuario_actual=Depends(solo_terapeutas)):
    sesion = functions.obtener_sesion_por_id(db, id_sesion)
    if not sesion:
        raise HTTPException(status_code=404, detail="Sesión no encontrada")
    functions.eliminar_sesion(db, sesion)
    return {"mensaje": f"La sesión con ID {id_sesion} ha sido eliminada"}

# Actualiza el estado, resultados o comentarios de una sesión
@app.put("/sesiones/{id_sesion}", response_model=schemas.SesionResponse)
def actualizar_sesion(id_sesion: int, datos: schemas.SesionUpdate, db: Session = Depends(get_db), usuario_actual=Depends(obtener_usuario_actual)):
    sesion = db.query(models.Sesion).filter(models.Sesion.id_sesion == id_sesion).first()
    
    if not sesion:
        raise HTTPException(status_code=404, detail="Sesión no encontrada")
    
    if datos.video_sesion is not None:
        sesion.video_sesion = datos.video_sesion
        sesion.estado = models.EstadoSesion.realizada 
        
    if datos.puntuacion is not None:
        sesion.puntuacion = datos.puntuacion
        sesion.estado = models.EstadoSesion.corregida 
        
    if datos.comentario_terapeuta is not None:
        sesion.comentario_terapeuta = datos.comentario_terapeuta

    if datos.estado is not None:
        sesion.estado = datos.estado

    if datos.tiempo_preparacion is not None:
        sesion.tiempo_preparacion = datos.tiempo_preparacion

    if datos.id_rutina is not None:
        rutina = db.query(models.Rutina).filter(models.Rutina.id_rutina == datos.id_rutina).first()
        if not rutina:
            raise HTTPException(status_code=404, detail="La nueva rutina indicada no existe")
        sesion.id_rutina = datos.id_rutina
        
    if datos.fecha is not None:
        sesion.fecha = datos.fecha

    try:
        db.commit()      
        db.refresh(sesion)
    except Exception as e:
        db.rollback()
        raise HTTPException(status_code=500, detail=f"Error al guardar en base de datos: {str(e)}")
            
    return sesion

# Sube la URL del vídeo de un ejercicio específico realizado en sesión
@app.put("/sesiones/{id_sesion}/ejercicio/{id_ejercicio}")
def subir_video_ejercicio(
    id_sesion: int, 
    id_ejercicio: int, 
    datos: schemas.SubirVideo, 
    db: Session = Depends(get_db)
):
    detalle = db.query(models.SesionEjercicio).filter(
        models.SesionEjercicio.id_sesion == id_sesion,
        models.SesionEjercicio.id_ejercicio == id_ejercicio
    ).first()
    
    if not detalle:
        raise HTTPException(status_code=404, detail="Ejercicio no encontrado en esta sesión")
        
    detalle.video_url = datos.video_url
    db.commit()
    
    return {"mensaje": "Vídeo guardado"}

# ========================================== 
#                 DASHBOARD                  
# ========================================== 

# Genera estadísticas globales del sistema para el panel de administración
@app.get("/dashboard/stats")
def obtener_estadisticas_sistema(db: Session = Depends(get_db), usuario_actual=Depends(solo_admins)):
    
    total_users = db.query(models.Usuario).count()
    n_admins = db.query(models.Usuario).filter(models.Usuario.rol == models.RolTipo.admin).count()
    n_terapeutas = db.query(models.Usuario).filter(models.Usuario.rol == models.RolTipo.terapeuta).count()
    n_pacientes = db.query(models.Usuario).filter(models.Usuario.rol == models.RolTipo.paciente).count()
    n_ejercicios = db.query(models.Ejercicio).count()
    n_fichas = db.query(models.FichaEjercicios).count()
    n_rutinas = db.query(models.Rutina).count()
    n_sesiones = db.query(models.Sesion).count()

    hoy = date.today()
    n_sesiones_hoy = db.query(models.Sesion).filter(func.date(models.Sesion.fecha) == hoy).count()

    n_pendientes = db.query(models.Sesion).filter(
        models.Sesion.video_sesion != None, 
        models.Sesion.puntuacion == None
    ).count()

    promedio = db.query(func.avg(models.Sesion.puntuacion)).scalar()

    if promedio is None:
        promedio = 0.0

    grafica_semanal = []
    for i in range(6, -1, -1):
        dia_objetivo = hoy - timedelta(days=i)
        count = db.query(models.Sesion).filter(func.date(models.Sesion.fecha) == dia_objetivo).count()
        grafica_semanal.append(count)

    logs_recientes = []
    ultimas_sesiones = db.query(models.Sesion).order_by(desc(models.Sesion.fecha)).limit(5).all()
    
    for sesion in ultimas_sesiones:
        paciente_nombre = "Paciente Desconocido"
        if sesion.id_paciente:
            p = db.query(models.Usuario).filter(models.Usuario.id_usuario == sesion.id_paciente).first()
            if p: paciente_nombre = f"{p.nombre} {p.apellidos}"

        tipo_log = "info"
        accion_texto = "Sesión programada"
        if sesion.estado == models.EstadoSesion.corregida:
            tipo_log = "exito"
            accion_texto = "Sesión corregida"
        elif sesion.estado == models.EstadoSesion.realizada:
            tipo_log = "alerta"
            accion_texto = "Sesión realizada (pendiente)"

        logs_recientes.append({
            "usuario": paciente_nombre,
            "accion": accion_texto,
            "tiempo": sesion.fecha.strftime("%d/%m %H:%M"),
            "tipo": tipo_log
        })

    return {
        "totalUsuarios": total_users,
        "admins": n_admins,
        "terapeutas": n_terapeutas,
        "pacientes": n_pacientes,
        "totalEjercicios": n_ejercicios,
        "totalFichas": n_fichas,
        "totalRutinas": n_rutinas,
        "totalSesiones": n_sesiones,
        "sesionesHoy": n_sesiones_hoy,
        "pendientes": n_pendientes,
        "notaMedia": round(promedio, 2),
        "grafica_semanal": grafica_semanal,
        "logs_recientes": logs_recientes
    }