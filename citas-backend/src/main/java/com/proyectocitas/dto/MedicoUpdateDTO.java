package com.proyectocitas.dto;

import jakarta.validation.constraints.*;
import com.proyectocitas.model.Medico;

public record MedicoUpdateDTO(

    @Positive(message = "El ID del usuario debe ser positivo")
    Long idUsuario,

    @Positive(message = "El ID de especialidad debe ser positivo")
    Long idEspecialidad,

    @Size(max = 50)
    @Pattern(
        regexp = "^[A-Za-z0-9-]+$",
        message = "El colegiado tiene un formato inválido"
    )
    String colegiado,

    @PositiveOrZero(message = "Los años no pueden ser negativos")
    Integer aniosExperiencia,

    @Size(max = 1000)
    String biografia,
        Medico.EstadoMedico estado

) {}