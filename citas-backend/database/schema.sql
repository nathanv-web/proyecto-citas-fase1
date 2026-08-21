-- =====================================================
-- SISTEMA DE CITAS MÉDICAS Y PORTAL DE SALUD
-- Base de Datos - Programación II
-- Archivo: schema.sql
-- JAIME DANILO VELASQUEZ MARTINEZ
-- =====================================================


-- =====================================================
-- 1. CREACIÓN DE LA BASE DE DATOS
-- =====================================================

CREATE DATABASE IF NOT EXISTS sistema_citas_medicas
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE sistema_citas_medicas;


-- =====================================================
-- 2. TABLA USUARIOS
-- =====================================================

CREATE TABLE usuarios (
    id_usuario BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    telefono VARCHAR(20),
    contrasena VARCHAR(255) NOT NULL,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    rol ENUM(
        'ROLE_ADMIN',
        'ROLE_DOCTOR',
        'ROLE_PATIENT'
    ) NOT NULL
);


-- =====================================================
-- 3. TABLA ESPECIALIDADES
-- =====================================================

CREATE TABLE especialidades (
    id_especialidad BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE,
    descripcion VARCHAR(255),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);


-- =====================================================
-- 4. TABLA DOCTORES
-- =====================================================

CREATE TABLE doctores (
    id_doctor BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL UNIQUE,
    id_especialidad BIGINT NOT NULL,
    colegiado VARCHAR(50) NOT NULL UNIQUE,
    anios_experiencia INT NOT NULL DEFAULT 0,
    biografia TEXT,

    estado ENUM(
        'ACTIVO',
        'INACTIVO'
    ) NOT NULL DEFAULT 'ACTIVO',

    CONSTRAINT fk_doctor_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_doctor_especialidad
        FOREIGN KEY (id_especialidad)
        REFERENCES especialidades(id_especialidad),

    CONSTRAINT chk_anios_experiencia
        CHECK (anios_experiencia >= 0)
);


-- =====================================================
-- 5. TABLA HORARIOS DISPONIBLES
-- =====================================================

CREATE TABLE horarios_disponibles (
    id_horario BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_doctor BIGINT NOT NULL,
    fecha DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,

    estado ENUM(
        'DISPONIBLE',
        'NO_DISPONIBLE'
    ) NOT NULL DEFAULT 'DISPONIBLE',

    CONSTRAINT fk_horario_doctor
        FOREIGN KEY (id_doctor)
        REFERENCES doctores(id_doctor),

    CONSTRAINT chk_horario_horas
        CHECK (hora_fin > hora_inicio),

    CONSTRAINT uq_horario_doctor
        UNIQUE (
            id_doctor,
            fecha,
            hora_inicio,
            hora_fin
        )
);


-- =====================================================
-- 6. TABLA ESTADOS DE CITA
-- =====================================================

CREATE TABLE estados_cita (
    id_estado BIGINT AUTO_INCREMENT PRIMARY KEY,

    nombre ENUM(
        'PENDIENTE',
        'COMPLETADA',
        'CANCELADA'
    ) NOT NULL UNIQUE,

    descripcion VARCHAR(255)
);


-- =====================================================
-- 7. TABLA CITAS MÉDICAS
-- =====================================================

CREATE TABLE citas_medicas (
    id_cita BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_horario BIGINT NOT NULL UNIQUE,
    id_usuario BIGINT NOT NULL,
    id_estado BIGINT NOT NULL,
    motivo VARCHAR(500) NOT NULL,
    diagnostico_receta TEXT,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,

    CONSTRAINT fk_cita_horario
        FOREIGN KEY (id_horario)
        REFERENCES horarios_disponibles(id_horario),

    CONSTRAINT fk_cita_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario),

    CONSTRAINT fk_cita_estado
        FOREIGN KEY (id_estado)
        REFERENCES estados_cita(id_estado)
);