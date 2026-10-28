-- Ejecutar una sola vez después de respaldar la base de datos.
-- Las cuentas existentes conservan su contraseña; solo nuevas cuentas administrativas son temporales.
ALTER TABLE usuarios
 ADD COLUMN debe_cambiar_password BOOLEAN NOT NULL DEFAULT FALSE,
 ADD COLUMN intentos_fallidos INT NOT NULL DEFAULT 0,
 ADD COLUMN bloqueado_hasta DATETIME NULL,
 ADD COLUMN version_sesion BIGINT NOT NULL DEFAULT 0;
-- Reversión (invalida esta versión del backend):
-- ALTER TABLE usuarios DROP COLUMN debe_cambiar_password, DROP COLUMN intentos_fallidos,
-- DROP COLUMN bloqueado_hasta, DROP COLUMN version_sesion;
