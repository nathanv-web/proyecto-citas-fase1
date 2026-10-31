CREATE TABLE IF NOT EXISTS fotos_perfil (
    id_usuario BIGINT NOT NULL PRIMARY KEY,
    tipo_contenido VARCHAR(32) NOT NULL,
    contenido LONGBLOB NOT NULL,
    actualizada_en DATETIME(6) NOT NULL,
    CONSTRAINT fk_foto_perfil_usuario FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
);
