-- Ejecutar una sola vez, antes de arrancar la nueva versión, sobre la base seleccionada y respaldada.
-- El estado de correo es informativo: no bloquea administradores ni cuentas existentes.
ALTER TABLE usuarios ADD COLUMN correo_verificado BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id_token BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_usuario BIGINT NOT NULL,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    correo VARCHAR(255) NOT NULL,
    creado_en DATETIME(6) NOT NULL,
    expira_en DATETIME(6) NOT NULL,
    usado_en DATETIME(6) NULL,
    INDEX idx_email_verification_usuario (id_usuario),
    INDEX idx_email_verification_expira (expira_en),
    CONSTRAINT fk_email_verification_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);
-- Reversión: DROP TABLE email_verification_tokens;
-- ALTER TABLE usuarios DROP COLUMN correo_verificado;
