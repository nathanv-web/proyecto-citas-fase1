-- =====================================================
-- SISTEMA DE CITAS MÉDICAS Y PORTAL DE SALUD
-- Base de Datos - Programación II
-- Archivo: schema.sql
-- =====================================================


-- =====================================================
-- 1. BASE DE DATOS
-- =====================================================

CREATE DATABASE IF NOT EXISTS citas_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE citas_db;


-- =====================================================
-- 2. USUARIOS
-- =====================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(255) NOT NULL,
    apellido VARCHAR(255) NOT NULL,
    correo VARCHAR(255) NOT NULL UNIQUE,
    telefono VARCHAR(255) NOT NULL,
    contrasena VARCHAR(255) NOT NULL,

    fecha_registro DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    activo BOOLEAN NOT NULL
        DEFAULT TRUE
);


-- =====================================================
-- 3. ROLES
-- =====================================================

CREATE TABLE IF NOT EXISTS roles (
    id_rol BIGINT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255)
);


-- =====================================================
-- 4. RELACIÓN USUARIO - ROL
-- =====================================================

CREATE TABLE IF NOT EXISTS usuario_rol (
    id_usuario BIGINT NOT NULL,
    id_rol BIGINT NOT NULL,

    PRIMARY KEY (id_usuario, id_rol),

    CONSTRAINT fk_usuario_rol_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_usuario_rol_rol
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol)
);


-- =====================================================
-- 5. ESPECIALIDADES
-- =====================================================

CREATE TABLE IF NOT EXISTS especialidades (
    id_especialidad BIGINT AUTO_INCREMENT PRIMARY KEY,

    nombre VARCHAR(255) NOT NULL UNIQUE,
    descripcion VARCHAR(255),

    activo BOOLEAN NOT NULL
        DEFAULT TRUE
);


-- =====================================================
-- 6. MÉDICOS
-- =====================================================

CREATE TABLE IF NOT EXISTS medicos (
    id_medico BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_usuario BIGINT NOT NULL UNIQUE,
    id_especialidad BIGINT NOT NULL,

    colegiado VARCHAR(255) NOT NULL UNIQUE,

    anios_experiencia INT
        NOT NULL DEFAULT 0,

    biografia TEXT,

    estado ENUM(
        'ACTIVO',
        'INACTIVO'
    ) NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT fk_medico_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_medico_especialidad
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad),

    CONSTRAINT chk_medico_experiencia
        CHECK (anios_experiencia >= 0)
);


-- =====================================================
-- 7. ENFERMEROS
-- Se conserva porque existe la entidad Enfermero.java
-- =====================================================

CREATE TABLE IF NOT EXISTS enfermeros (
    id_enfermero BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_usuario BIGINT UNIQUE,

    colegiado VARCHAR(255),
    area VARCHAR(255),
    turno VARCHAR(255),
    codigo_empleado VARCHAR(255),

    CONSTRAINT fk_enfermero_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);


-- =====================================================
-- 8. SECRETARIAS
-- Se conserva porque existe la entidad Secretaria.java
-- =====================================================

CREATE TABLE IF NOT EXISTS secretarias (
    id_secretaria BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_usuario BIGINT UNIQUE,

    codigo_empleado VARCHAR(255),
    area VARCHAR(255),
    turno VARCHAR(255),

    CONSTRAINT fk_secretaria_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);


-- =====================================================
-- 9. HORARIOS DISPONIBLES
-- =====================================================

CREATE TABLE IF NOT EXISTS horarios_disponibles (
    id_horario BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_medico BIGINT NOT NULL,

    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,

    estado ENUM(
        'DISPONIBLE',
        'RESERVADO',
        'NO_DISPONIBLE'
    ) NOT NULL DEFAULT 'DISPONIBLE',

    CONSTRAINT fk_horario_medico
        FOREIGN KEY (id_medico)
        REFERENCES medicos(id_medico),

    CONSTRAINT chk_horario_horas
        CHECK (hora_fin > hora_inicio),

    CONSTRAINT uq_horario_medico
        UNIQUE (
            id_medico,
            fecha,
            hora_inicio,
            hora_fin
        )
);


-- =====================================================
-- 10. ESTADOS DE CITA
-- =====================================================

CREATE TABLE IF NOT EXISTS estados_cita (
    id_estado BIGINT AUTO_INCREMENT PRIMARY KEY,

    nombre ENUM(
        'PENDIENTE',
        'COMPLETADA',
        'CANCELADA'
    ) NOT NULL UNIQUE,

    descripcion VARCHAR(255)
);


-- =====================================================
-- 11. CITAS MÉDICAS
-- =====================================================

CREATE TABLE IF NOT EXISTS citas_medicas (
    id_cita BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_horario BIGINT NOT NULL UNIQUE,
    id_paciente BIGINT NOT NULL,
    id_estado BIGINT NOT NULL,

    motivo VARCHAR(500) NOT NULL,

    diagnostico_receta TEXT,

    fecha_creacion DATETIME NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    observaciones TEXT,

    CONSTRAINT fk_cita_horario
        FOREIGN KEY (id_horario)
        REFERENCES horarios_disponibles(id_horario),

    CONSTRAINT fk_cita_paciente
        FOREIGN KEY (id_paciente)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_cita_estado
        FOREIGN KEY (id_estado)
        REFERENCES estados_cita(id_estado)
);