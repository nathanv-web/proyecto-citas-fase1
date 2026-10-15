package com.proyectocitas.dto;

import jakarta.validation.constraints.NotNull;

public record EspecialidadEstadoDTO(
        @NotNull(message = "El estado de la especialidad es obligatorio") Boolean activo
) { }
