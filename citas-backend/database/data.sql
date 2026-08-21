USE sistema_citas_medicas;

-- =====================================================
-- ESTADOS DE CITA
-- =====================================================

INSERT INTO estados_cita (nombre, descripcion)
VALUES
('PENDIENTE', 'La cita fue registrada y aún no ha sido atendida'),
('COMPLETADA', 'La consulta médica ya fue realizada'),
('CANCELADA', 'La cita fue cancelada');


-- =====================================================
-- USUARIOS
-- =====================================================

INSERT INTO usuarios
(nombre, apellido, correo, telefono, contrasena, activo, rol)
VALUES
('Ana', 'Lopez', 'admin@clinica.com', '55550001', '123456', TRUE, 'ROLE_ADMIN'),
('Carlos', 'Martinez', 'doctor@clinica.com', '55550002', '123456', TRUE, 'ROLE_DOCTOR'),
('Maria', 'Perez', 'paciente@correo.com', '55550003', '123456', TRUE, 'ROLE_PATIENT');


-- =====================================================
-- ESPECIALIDADES
-- =====================================================

INSERT INTO especialidades
(nombre, descripcion, activo)
VALUES
('Medicina General', 'Atención médica general para pacientes', TRUE),
('Cardiología', 'Especialidad enfocada en enfermedades del corazón', TRUE),
('Pediatría', 'Atención médica especializada para niños', TRUE);


-- =====================================================
-- DOCTOR DE PRUEBA
-- =====================================================

INSERT INTO doctores
(id_usuario, id_especialidad, colegiado, anios_experiencia, biografia, estado)
VALUES
(
    2,
    1,
    'COL-1001',
    8,
    'Médico con experiencia en atención general y consulta ambulatoria',
    'ACTIVO'
);


-- =====================================================
-- HORARIOS DISPONIBLES
-- =====================================================

INSERT INTO horarios_disponibles
(id_doctor, fecha, hora_inicio, hora_fin, estado)
VALUES
(1, '2026-08-24', '08:00:00', '08:30:00', 'DISPONIBLE'),
(1, '2026-08-24', '08:30:00', '09:00:00', 'DISPONIBLE'),
(1, '2026-08-24', '09:00:00', '09:30:00', 'DISPONIBLE');


-- =====================================================
-- CITA MÉDICA DE PRUEBA
-- =====================================================

INSERT INTO citas_medicas
(id_horario, id_usuario, id_estado, motivo, diagnostico_receta, observaciones)
VALUES
(
    1,
    3,
    1,
    'Dolor de cabeza frecuente',
    NULL,
    'Primera consulta del paciente'
);