-- No contiene credenciales ni tokens originales. Ejecutar una sola vez antes de arrancar la nueva versión.
-- La versión optimista evita que una edición antigua restaure una contraseña/sesión ya revocada.
ALTER TABLE usuarios ADD COLUMN version_registro BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    correo VARCHAR(255) NOT NULL,
    version_sesion BIGINT NOT NULL,
    creado_en DATETIME(6) NOT NULL,
    expira_en DATETIME(6) NOT NULL,
    usado_en DATETIME(6) NULL,
    INDEX idx_password_reset_usuario (id_usuario),
    INDEX idx_password_reset_expira (expira_en),
    CONSTRAINT fk_password_reset_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);
-- Limpieza de mantenimiento (conservar auditoría según la política de la instalación):
-- DELETE FROM password_reset_tokens WHERE expira_en < UTC_TIMESTAMP() - INTERVAL 7 DAY;
-- Reversión: DROP TABLE password_reset_tokens;
