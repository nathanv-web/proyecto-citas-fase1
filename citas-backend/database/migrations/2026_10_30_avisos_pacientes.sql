-- Ejecutar después de 2026_10_29_notificaciones.sql en instalaciones existentes.
-- La marca se restablece al reprogramar la cita.
ALTER TABLE citas_medicas
    ADD COLUMN IF NOT EXISTS recordatorio_enviado BOOLEAN NOT NULL DEFAULT FALSE;

-- Admite los nuevos tipos tanto en tablas creadas por SQL como por Hibernate.
ALTER TABLE notificaciones MODIFY COLUMN tipo VARCHAR(30) NOT NULL;
