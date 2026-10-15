-- Migración puntual para una instalación existente.
-- Ejecutar DESPUÉS de reiniciar la API con el enum Permiso actualizado.
-- Hibernate debe haber creado/actualizado rol_permiso previamente.
-- Seleccionar la base correcta al invocar mariadb; no se cambia de base aquí.
-- No ejecutar en cada arranque: no deben restaurarse permisos revocados después.
-- Conserva todas las asignaciones existentes y no modifica roles personalizados.

INSERT INTO rol_permiso (id_rol, permiso)
SELECT r.id_rol, nuevos.permiso
FROM roles r
JOIN (
    SELECT 'ROLE_ADMIN' AS nombre, 'ACCESO_ADMIN' AS permiso
    UNION ALL SELECT 'ROLE_DOCTOR', 'ACCESO_MEDICO'
    UNION ALL SELECT 'ROLE_PATIENT', 'ACCESO_PACIENTE'
    UNION ALL SELECT 'ROLE_ADMIN', 'ACTUALIZAR_MEDICOS'
    UNION ALL SELECT 'ROLE_ADMIN', 'ELIMINAR_MEDICOS'
) nuevos ON nuevos.nombre = r.nombre
WHERE NOT EXISTS (
    SELECT 1 FROM rol_permiso existentes
    WHERE existentes.id_rol = r.id_rol AND existentes.permiso = nuevos.permiso
);
