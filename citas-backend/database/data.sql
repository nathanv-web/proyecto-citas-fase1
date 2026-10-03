USE citas_db;


-- =====================================================
-- 1. ROLES
-- =====================================================

INSERT IGNORE INTO roles
(nombre, descripcion)
VALUES
(
    'ROLE_ADMIN',
    'Administrador del sistema'
),
(
    'ROLE_DOCTOR',
    'Médico del sistema'
),
(
    'ROLE_PATIENT',
    'Paciente del sistema'
);


-- =====================================================
-- 2. ESTADOS DE CITA
-- =====================================================

INSERT IGNORE INTO estados_cita
(nombre, descripcion)
VALUES
(
    'PENDIENTE',
    'La cita fue registrada y aún no ha sido atendida'
),
(
    'COMPLETADA',
    'La consulta médica ya fue realizada'
),
(
    'CANCELADA',
    'La cita fue cancelada'
);


-- =====================================================
-- 3. ESPECIALIDADES
-- =====================================================

INSERT IGNORE INTO especialidades
(nombre, descripcion, activo)
VALUES
(
    'Medicina General',
    'Atención médica general para pacientes',
    TRUE
),
(
    'Cardiología',
    'Especialidad enfocada en enfermedades del corazón',
    TRUE
),
(
    'Pediatría',
    'Atención médica especializada para niños',
    TRUE
);


-- =====================================================
-- 4. USUARIOS DE PRUEBA
--
-- Contraseña de los tres usuarios:
-- 123456
--
-- La contraseña está almacenada usando BCrypt
-- =====================================================

INSERT IGNORE INTO usuarios
(
    nombre,
    apellido,
    correo,
    telefono,
    contrasena,
    fecha_registro,
    activo
)
VALUES
(
    'Ana',
    'Lopez',
    'admin@clinica.com',
    '55550001',
    '$2y$10$EPWI68RqdaVaZmZJFSPexeJQlc6ZduqFCtpjYPq3XMFJ4gqAwXp1W',
    NOW(),
    TRUE
),
(
    'Carlos',
    'Martinez',
    'doctor@clinica.com',
    '55550002',
    '$2y$10$EPWI68RqdaVaZmZJFSPexeJQlc6ZduqFCtpjYPq3XMFJ4gqAwXp1W',
    NOW(),
    TRUE
),
(
    'Maria',
    'Perez',
    'paciente@correo.com',
    '55550003',
    '$2y$10$EPWI68RqdaVaZmZJFSPexeJQlc6ZduqFCtpjYPq3XMFJ4gqAwXp1W',
    NOW(),
    TRUE
);


-- =====================================================
-- 5. ASIGNACIÓN DE ROLES
-- =====================================================


-- ADMIN

INSERT IGNORE INTO usuario_rol
(id_usuario, id_rol)

SELECT
    u.id_usuario,
    r.id_rol

FROM usuarios u
CROSS JOIN roles r

WHERE u.correo = 'admin@clinica.com'
AND r.nombre = 'ROLE_ADMIN';


-- MÉDICO

INSERT IGNORE INTO usuario_rol
(id_usuario, id_rol)

SELECT
    u.id_usuario,
    r.id_rol

FROM usuarios u
CROSS JOIN roles r

WHERE u.correo = 'doctor@clinica.com'
AND r.nombre = 'ROLE_DOCTOR';


-- PACIENTE

INSERT IGNORE INTO usuario_rol
(id_usuario, id_rol)

SELECT
    u.id_usuario,
    r.id_rol

FROM usuarios u
CROSS JOIN roles r

WHERE u.correo = 'paciente@correo.com'
AND r.nombre = 'ROLE_PATIENT';


-- =====================================================
-- 6. MÉDICO DE PRUEBA
-- =====================================================

INSERT IGNORE INTO medicos
(
    id_usuario,
    id_especialidad,
    colegiado,
    anios_experiencia,
    biografia,
    estado
)

SELECT
    u.id_usuario,
    e.id_especialidad,
    'COL-1001',
    8,
    'Médico con experiencia en atención general y consulta ambulatoria',
    'ACTIVO'

FROM usuarios u
CROSS JOIN especialidades e

WHERE u.correo = 'doctor@clinica.com'
AND e.nombre = 'Medicina General';


-- =====================================================
-- 7. HORARIOS DEL MÉDICO
--
-- Se crean para el día siguiente a la ejecución
-- del script.
--
-- Primer horario: RESERVADO porque tendrá una cita.
-- Los demás quedan DISPONIBLES.
-- =====================================================


-- 08:00 - 08:30 RESERVADO

INSERT IGNORE INTO horarios_disponibles
(
    id_medico,
    fecha,
    hora_inicio,
    hora_fin,
    estado
)

SELECT
    m.id_medico,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    '08:00:00',
    '08:30:00',
    'RESERVADO'

FROM medicos m
JOIN usuarios u
    ON u.id_usuario = m.id_usuario

WHERE u.correo = 'doctor@clinica.com';


-- 08:30 - 09:00 DISPONIBLE

INSERT IGNORE INTO horarios_disponibles
(
    id_medico,
    fecha,
    hora_inicio,
    hora_fin,
    estado
)

SELECT
    m.id_medico,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    '08:30:00',
    '09:00:00',
    'DISPONIBLE'

FROM medicos m
JOIN usuarios u
    ON u.id_usuario = m.id_usuario

WHERE u.correo = 'doctor@clinica.com';


-- 09:00 - 09:30 DISPONIBLE

INSERT IGNORE INTO horarios_disponibles
(
    id_medico,
    fecha,
    hora_inicio,
    hora_fin,
    estado
)

SELECT
    m.id_medico,
    DATE_ADD(CURDATE(), INTERVAL 1 DAY),
    '09:00:00',
    '09:30:00',
    'DISPONIBLE'

FROM medicos m
JOIN usuarios u
    ON u.id_usuario = m.id_usuario

WHERE u.correo = 'doctor@clinica.com';


-- =====================================================
-- 8. CITA DE PRUEBA
--
-- Paciente: paciente@correo.com
-- Médico: doctor@clinica.com
-- Estado: PENDIENTE
-- =====================================================

INSERT IGNORE INTO citas_medicas
(
    id_horario,
    id_paciente,
    id_estado,
    motivo,
    diagnostico_receta,
    fecha_creacion,
    observaciones
)

SELECT
    h.id_horario,
    paciente.id_usuario,
    ec.id_estado,
    'Dolor de cabeza frecuente',
    NULL,
    NOW(),
    'Primera consulta del paciente'

FROM horarios_disponibles h

JOIN medicos m
    ON m.id_medico = h.id_medico

JOIN usuarios medico_usuario
    ON medico_usuario.id_usuario = m.id_usuario

CROSS JOIN usuarios paciente

CROSS JOIN estados_cita ec

WHERE medico_usuario.correo = 'doctor@clinica.com'

AND paciente.correo = 'paciente@correo.com'

AND ec.nombre = 'PENDIENTE'

AND h.fecha = DATE_ADD(CURDATE(), INTERVAL 1 DAY)

AND h.hora_inicio = '08:00:00';