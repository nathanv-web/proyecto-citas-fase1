-- ================================================
-- CITANOVA
-- MODULO DE NOTIFICACIONES
-- Integrante 2 - Backend
-- ================================================

CREATE TABLE IF NOT EXISTS notificaciones (

    id_notificacion BIGINT AUTO_INCREMENT PRIMARY KEY,

    id_destinatario BIGINT NOT NULL,

    titulo VARCHAR(150) NOT NULL,

    mensaje VARCHAR(500) NOT NULL,

    tipo VARCHAR(30) NOT NULL,

    leida BOOLEAN NOT NULL DEFAULT FALSE,

    fecha_creacion DATETIME(6) NOT NULL
        DEFAULT CURRENT_TIMESTAMP(6),

    CONSTRAINT fk_notificacion_destinatario
        FOREIGN KEY (id_destinatario)
        REFERENCES usuarios(id_usuario),

    INDEX idx_notif_destinatario_fecha (
        id_destinatario,
        fecha_creacion
    ),

    INDEX idx_notif_destinatario_leida (
        id_destinatario,
        leida
    )
);
