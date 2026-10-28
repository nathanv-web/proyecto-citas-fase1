package com.proyectocitas.dto.request;

import jakarta.validation.constraints.NotNull;

public record EspecialidadEstadoRequestDTO(
        @NotNull(message = "El estado de la especialidad es obligatorio") Boolean activo
) { }
