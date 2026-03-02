
DROP TABLE IF EXISTS evaluacion CASCADE;
DROP TABLE IF EXISTS sesion CASCADE;
DROP TABLE IF EXISTS ficha_ejercicios CASCADE;
DROP TABLE IF EXISTS ejercicio CASCADE;
DROP TABLE IF EXISTS rutina CASCADE;
DROP TABLE IF EXISTS usuario CASCADE;

DROP TYPE IF EXISTS zona_tipo CASCADE;
DROP TYPE IF EXISTS rol_tipo CASCADE;

DROP TYPE IF EXISTS estado_sesion CASCADE; 


CREATE TYPE rol_tipo AS ENUM ('admin', 'terapeuta', 'paciente');
CREATE TYPE zona_tipo AS ENUM ('Torso', 'Cabeza', 'Extremidades_Superiores', 'Extremidades_Inferiores');
CREATE TYPE estado_sesion AS ENUM ('pendiente', 'realizada', 'corregida'); -- NUEVO ENUM

-- 3. TABLA USUARIO
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

-- 4. TABLA RUTINA
CREATE TABLE rutina (
    id_rutina SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500) NOT NULL,
    id_terapeuta INT NOT NULL,
    
    CONSTRAINT fk_rutina_terapeuta FOREIGN KEY (id_terapeuta) REFERENCES usuario(id_usuario)
);

-- 5. TABLA EJERCICIO 
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

-- 6. TABLA FICHA
CREATE TABLE ficha_ejercicios (
    id_ficha_ejercicios SERIAL PRIMARY KEY,
    nombre VARCHAR(100), 
    
    -- Campos del cronómetro
    series INT,
    repeticiones INT,
    tiempo_minutos INT, 
    
    id_rutina INT NOT NULL,
    id_ejercicio INT NOT NULL, 
    
    CONSTRAINT fk_ficha_rutina FOREIGN KEY (id_rutina) REFERENCES rutina(id_rutina) ON DELETE CASCADE,
    CONSTRAINT fk_ficha_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES ejercicio(id_ejercicio)
);

-- 7. TABLA SESION 
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
-- 8. TABLA SESION_EJERCICIOS
CREATE TABLE sesion_ejercicios (
    id SERIAL PRIMARY KEY,
    id_sesion INT NOT NULL,
    id_ejercicio INT NOT NULL,
    video_url VARCHAR(255),
    
    CONSTRAINT fk_detalle_sesion FOREIGN KEY (id_sesion) REFERENCES sesion(id_sesion) ON DELETE CASCADE,
    CONSTRAINT fk_detalle_ejercicio FOREIGN KEY (id_ejercicio) REFERENCES ejercicio(id_ejercicio)
);
-- INSERTAR ADMIN 
INSERT INTO usuario (dni, correo_electronico, nombre, apellidos, contrasena, rol)
VALUES (
    1, 
    'admin@hospital.com', 
    'Super', 
    'Admin', 
    -- Hash válido de "admin" generado con bcrypt
    '$2b$12$PvaX2O9dA3EeGLfuCHrFu.MTFDCFLjUQxm/2sgKdVXyWSoIgGSJwy', 
    'admin'
);