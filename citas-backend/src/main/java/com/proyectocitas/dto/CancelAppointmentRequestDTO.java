package com.proyectocitas.dto;

import jakarta.validation.constraints.Size;

public class CancelAppointmentRequestDTO {

    @Size(
        max = 500,
        message = "El motivo de cancelacion no puede superar 500 caracteres"
    )
    private String motivoCancelacion;

    public CancelAppointmentRequestDTO() {
    }

    public String getMotivoCancelacion() {
        return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
        this.motivoCancelacion = motivoCancelacion;
    }
}