package com.proyectocitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class AppointmentRequestDTO {

    //ID del horario que el paciente desea reservar
    @NotNull(message = "El horario es obligatorio")
    @Positive(message = "El ID del horario debe ser mayor que cero")
    private Long idHorario;

    //Motivo por el cual el paciente solicita la cita
    @NotBlank(message = "El motivo de la cita es obligatorio")
    @Size(
        min = 5,
        max = 500,
        message = "El motivo debe tener entre 5 y 500 caracteres"
    )
    private String motivo;


    public AppointmentRequestDTO() {
    }


    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Long idHorario) {
        this.idHorario = idHorario;
    }


    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}