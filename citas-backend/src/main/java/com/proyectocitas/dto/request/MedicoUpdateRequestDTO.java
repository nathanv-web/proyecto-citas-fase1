package com.proyectocitas.dto.request;

import jakarta.validation.constraints.*;
import com.proyectocitas.model.Medico;

public record MedicoUpdateRequestDTO(

    @Positive(message = "El ID del usuario debe ser positivo")
    Long idUsuario,

    @Positive(message = "El ID de especialidad debe ser positivo")
    Long idEspecialidad,

    @Size(max = 50)
    @Pattern(
        regexp = "^COL-[0-9]{4}$",
        message = "El colegiado debe tener el formato COL-0000 (cuatro dígitos)"
    )
    String colegiado,

    @PositiveOrZero(message = "Los años no pueden ser negativos")
    Integer aniosExperiencia,

    @Size(max = 1000)
    String biografia,
        Medico.EstadoMedico estado

) {}
