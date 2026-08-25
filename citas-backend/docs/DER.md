# Diagrama Entidad-Relación - Sistema de Citas Médicas

El siguiente diagrama representa el modelo activo utilizado por el backend en la Fase 1.

```mermaid
erDiagram
    ROLES ||--o{ USUARIOS : asigna
    ESPECIALIDADES ||--o{ MEDICOS : clasifica
    MEDICOS ||--o{ HORARIOS_DISPONIBLES : publica
    USUARIOS ||--o{ CITAS_MEDICAS : solicita
    HORARIOS_DISPONIBLES ||--o| CITAS_MEDICAS : reserva

    ROLES {
        BIGINT id PK
        VARCHAR nombre UK
    }

    USUARIOS {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR email UK
        VARCHAR password
        BIGINT rol_id FK
    }

    ESPECIALIDADES {
        BIGINT id PK
        VARCHAR nombre UK
        VARCHAR descripcion
    }

    MEDICOS {
        BIGINT id PK
        VARCHAR nombre
        VARCHAR apellido
        VARCHAR colegiado UK
        VARCHAR telefono
        BIGINT especialidad_id FK
    }

    HORARIOS_DISPONIBLES {
        BIGINT id PK
        BIGINT medico_id FK
        DATE fecha
        TIME hora_inicio
        TIME hora_fin
        BOOLEAN disponible
    }

    CITAS_MEDICAS {
        BIGINT id_cita PK
        BIGINT id_horario FK,UK
        BIGINT id_usuario FK
        BIGINT id_estado
        VARCHAR motivo
        TEXT diagnostico_receta
        DATETIME fecha_creacion
        TEXT observaciones
    }
```

## Relaciones principales

- Un **rol** puede estar asignado a muchos usuarios.
- Una **especialidad** puede pertenecer a muchos médicos.
- Un **médico** puede publicar muchos horarios disponibles.
- Un **usuario** puede solicitar varias citas médicas.
- Un **horario disponible** puede quedar asociado como máximo a una cita; al reservarse, el backend lo marca como no disponible.

> Nota: en la implementación actual de `CitaMedica`, `idHorario`, `idUsuario` e `idEstado` se almacenan como identificadores numéricos. Las relaciones del diagrama muestran la asociación lógica utilizada por el sistema en esta fase.
