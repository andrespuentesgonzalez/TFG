-- 1. LIMPIEZA (Orden correcto para evitar errores de dependencia)
DROP TABLE IF EXISTS sesion_ejercicios CASCADE;
DROP TABLE IF EXISTS sesion CASCADE;
DROP TABLE IF EXISTS ficha_ejercicios CASCADE;
DROP TABLE IF EXISTS ejercicio CASCADE;
DROP TABLE IF EXISTS rutina CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;
DROP TYPE IF EXISTS zona_tipo CASCADE;
DROP TYPE IF EXISTS rol_tipo CASCADE;
DROP TYPE IF EXISTS estado_sesion CASCADE;

-- 2. DEFINICIÓN DE TIPOS
CREATE TYPE rol_tipo AS ENUM ('admin', 'terapeuta', 'paciente');
CREATE TYPE zona_tipo AS ENUM ('Torso', 'Cabeza', 'Extremidades_Superiores', 'Extremidades_Inferiores');
CREATE TYPE estado_sesion AS ENUM ('pendiente', 'realizada', 'corregida');

-- 3. CREACIÓN DE TABLAS (Sin cambios en tu estructura)
CREATE TABLE usuario (
    id_usuario SERIAL PRIMARY KEY,
    dni INT UNIQUE NOT NULL,
    correo_electronico VARCHAR(100) UNIQUE NOT NULL,
    nombre VARCHAR(50) NOT NULL,
    apellidos VARCHAR(50) NOT NULL,
    contrasena VARCHAR(100) NOT NULL,
    rol rol_tipo NOT NULL,
    id_terapeuta_asignado INT, 
    CONSTRAINT fk_usuario_terapeuta FOREIGN KEY (id_terapeuta_asignado) REFERENCES usuario(id_usuario)
);

CREATE TABLE rutina (
    id_rutina SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    id_terapeuta INT NOT NULL,
    CONSTRAINT fk_rutina_terapeuta FOREIGN KEY (id_terapeuta) REFERENCES usuario(id_usuario)
);

CREATE TABLE ejercicio (
    id_ejercicio SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    duracion INT NOT NULL,
    video_prueba VARCHAR(255),
    zona_afectada zona_tipo NOT NULL, 
    id_terapeuta INT NOT NULL,
    CONSTRAINT fk_ejercicio_terapeuta FOREIGN KEY (id_terapeuta) REFERENCES usuario(id_usuario)
);

CREATE TABLE ficha_ejercicios (
    id_ficha_ejercicios SERIAL PRIMARY KEY,
    nombre VARCHAR(100), 
    series INT,
    repeticiones INT,
    tiempo_minutos INT, 
    id_rutina INT NOT NULL,
    id_ejercicio INT NOT NULL, 
    CONSTRAINT fk_ficha_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina) ON DELETE CASCADE,
    CONSTRAINT fk_ficha_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES ejercicio(id_ejercicio)
);

CREATE TABLE sesion (
    id_sesion SERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    id_rutina INT, 
    id_paciente INT NOT NULL,
    id_terapeuta INT NOT NULL,
    estado estado_sesion DEFAULT 'pendiente' NOT NULL,
    tiempo_preparacion INT DEFAULT 10 NOT NULL, 
    video_sesion VARCHAR(255),
    comentario_paciente VARCHAR(255),
    puntuacion INT,
    comentario_terapeuta VARCHAR(255),
    CONSTRAINT fk_sesion_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina),
    CONSTRAINT fk_sesion_paciente FOREIGN KEY (id_paciente) REFERENCES usuario(id_usuario),
    CONSTRAINT fk_sesion_terapeuta FOREIGN KEY (id_terapeuta) REFERENCES usuario(id_usuario)
);

CREATE TABLE sesion_ejercicios (
    id SERIAL PRIMARY KEY,
    id_sesion INT NOT NULL,
    id_ejercicio INT NOT NULL,
    video_url VARCHAR(255),
    CONSTRAINT fk_detalle_sesion FOREIGN KEY (id_sesion) REFERENCES sesion(id_sesion) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES ejercicio(id_ejercicio)
);

-- 4. INSERCIÓN DE DATOS (Orden lógico: Staff -> Pacientes -> Contenido -> Sesiones)

-- Admin (ID 1)
INSERT INTO usuario (dni, correo_electronico, nombre, apellidos, contrasena, rol)
VALUES (1, 'admin@hospital.com', 'Super', 'Admin', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'admin');

-- Terapeutas (IDs 2, 3, 4)
INSERT INTO usuario (dni, correo_electronico, nombre, apellidos, contrasena, rol) VALUES
(101, 'ana.terapeuta@hospital.com', 'Ana', 'Martínez Martínez', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'terapeuta'),
(102, 'pedro.terapeuta@hospital.com', 'Pedro', 'Ortega de Llanos', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'terapeuta'),
(103, 'lucia.terapeuta@hospital.com', 'Lucía', 'Gómez Gómez', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'terapeuta');

-- Pacientes (Asignados a los IDs creados arriba)
INSERT INTO usuario (dni, correo_electronico, nombre, apellidos, contrasena, rol, id_terapeuta_asignado) VALUES
(201, 'paciente1@correo.com', 'Roberto', 'Lopez Álvarez', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'paciente', 2),
(202, 'paciente2@correo.com', 'Elena', 'Fernandez Alves', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'paciente', 2),
(203, 'paciente3@correo.com', 'Mario', 'Martinez Serrano', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'paciente', 3),
(204, 'paciente4@correo.com', 'Laura', 'Garcia Garcia', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'paciente', 2),
(205, 'paciente5@correo.com', 'Diego', 'Santos de Miguel', '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 'paciente', null);

-- Ejercicios Parkinson
INSERT INTO ejercicio (nombre, descripcion, duracion, video_prueba, zona_afectada, id_terapeuta) VALUES
('Alcance de Estrellas', 'Estirar un brazo hacia el techo abriendo la mano.', 300, 'vid_01.mp4', 'Extremidades_Superiores', 2),
('Marcha en el Sitio', 'Levantar rodillas rítmicamente.', 240, 'vid_02.mp4', 'Extremidades_Inferiores', 2),
('Oposición del Pulgar', 'Tocar cada dedo con el pulgar.', 180, 'vid_03.mp4', 'Extremidades_Superiores', 2),
('Giro de Tronco en Silla', 'Girar torso suavemente.', 300, 'vid_04.mp4', 'Torso', 2);

-- Rutinas
INSERT INTO rutina (nombre, descripcion, id_terapeuta) VALUES
('Movilidad Matinal', 'Ejercicios para rigidez.', 2),
('Coordinación y Marcha', 'Caminata y manos.', 2),
('Circuito Completo', 'Amplitud general.', 2);

-- Fichas (Relación Rutina-Ejercicio)
INSERT INTO ficha_ejercicios (nombre, series, repeticiones, tiempo_minutos, id_rutina, id_ejercicio) VALUES
('Ficha 1', 3, 8, 5, 1, 4), ('Ficha 2', 3, 10, 5, 1, 1),
('Ficha 3', 3, 15, 10, 2, 2), ('Ficha 4', 4, 12, 5, 2, 3),
('Ficha 5', 2, 10, 5, 3, 1), ('Ficha 6', 3, 20, 10, 3, 2);

-- Sesiones para Roberto (Paciente ID 5)
INSERT INTO sesion (fecha, id_rutina, id_paciente, id_terapeuta, estado, puntuacion, comentario_paciente, comentario_terapeuta) VALUES
('2026-02-01 10:00:00', 1, 5, 2, 'corregida', 8, '', ''),
('2026-02-05 10:00:00', 2, 5, 2, 'realizada', NULL, '', ''),
('2026-02-15 10:00:00', 3, 5, 2, 'pendiente', NULL, '', '');

-- Registro de ejercicio en la sesión corregida
INSERT INTO sesion_ejercicios (id_sesion, id_ejercicio, video_url) VALUES (1, 1, 'video_ej_hecho.mp4');

-- 1. Sesiones para Roberto Lopez (ID 5) - Ya tiene algunas, añadimos más historial
INSERT INTO sesion (fecha, id_rutina, id_paciente, id_terapeuta, estado, puntuacion, comentario_paciente, comentario_terapeuta, video_sesion) VALUES
('2026-01-20 09:00:00', 1, 5, 2, 'corregida', 7, '', '', 'http://vid.com/roberto_1.mp4'),
('2026-01-25 09:30:00', 1, 5, 2, 'corregida', 8, '', '', 'http://vid.com/roberto_2.mp4'),
('2026-01-28 10:00:00', 2, 5, 2, 'corregida', 9, '', '', 'http://vid.com/roberto_3.mp4');

-- 2. Sesiones para Elena Fernandez (ID 6) - Historial de la semana pasada
INSERT INTO sesion (fecha, id_rutina, id_paciente, id_terapeuta, estado, puntuacion, comentario_paciente, comentario_terapeuta, video_sesion) VALUES
('2026-02-02 11:00:00', 1, 6, 2, 'corregida', 6, '', '', 'http://vid.com/elena_1.mp4'),
('2026-02-04 11:00:00', 1, 6, 2, 'corregida', 7, '', '', 'http://vid.com/elena_2.mp4'),
('2026-02-06 11:00:00', 2, 6, 2, 'realizada', NULL, '', '', 'http://vid.com/elena_3.mp4'),
('2026-02-14 11:00:00', 2, 6, 2, 'pendiente', NULL, '', '', NULL);

-- 3. Sesiones para Mario Martinez (ID 7) - Asignado a Lucía (Terapeuta ID 4)
INSERT INTO sesion (fecha, id_rutina, id_paciente, id_terapeuta, estado, puntuacion, comentario_paciente, comentario_terapeuta, video_sesion) VALUES
('2026-02-01 17:00:00', 3, 7, 4, 'corregida', 10, '', '', 'http://vid.com/mario_1.mp4'),
('2026-02-05 17:00:00', 3, 7, 4, 'corregida', 9, '', '', 'http://vid.com/mario_2.mp4'),
('2026-02-10 17:00:00', 3, 7, 4, 'realizada', NULL, '', '', 'http://vid.com/mario_3.mp4'),
('2026-02-15 17:00:00', 1, 7, 4, 'pendiente', NULL, '', '', NULL);

-- 4. Sesiones para Laura Garcia (ID 8) - Solo una pendiente para hoy
INSERT INTO sesion (fecha, id_rutina, id_paciente, id_terapeuta, estado, tiempo_preparacion) VALUES
('2026-02-12 12:00:00', 2, 8, 2, 'pendiente', 10);

-- 5. Llenamos detalles de ejercicios para algunas de estas sesiones (inflar sesion_ejercicios)
-- Usamos IDs de sesión del 4 al 10 (las que acabamos de insertar)
INSERT INTO sesion_ejercicios (id_sesion, id_ejercicio, video_url) VALUES 
(4, 1, 'v_ej_1.mp4'), (4, 4, 'v_ej_4.mp4'),
(5, 1, 'v_ej_1.mp4'), (5, 4, 'v_ej_4.mp4'),
(7, 4, 'v_ej_4.mp4'), (8, 4, 'v_ej_4.mp4'),
(11, 1, 'v_ej_1.mp4'), (11, 2, 'v_ej_2.mp4'), (11, 3, 'v_ej_3.mp4');

-- Ver resultados
SELECT * FROM usuario;
select * from sesion;