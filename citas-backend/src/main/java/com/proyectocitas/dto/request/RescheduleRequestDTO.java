package com.proyectocitas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class RescheduleRequestDTO {

    @NotNull(message = "El nuevo horario es obligatorio")
    @Positive(message = "El ID del horario debe ser positivo")
    private Long idHorarioNuevo;

    public RescheduleRequestDTO() {
    }

    public Long getIdHorarioNuevo() {
        return idHorarioNuevo;
    }

    public void setIdHorarioNuevo(Long idHorarioNuevo) {
        this.idHorarioNuevo = idHorarioNuevo;
    }
}