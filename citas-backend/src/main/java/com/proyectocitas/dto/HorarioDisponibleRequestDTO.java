package com.proyectocitas.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.time.LocalTime;

public class HorarioDisponibleRequestDTO {

    //ID del medico al que pertenece el horario
    @NotNull(message = "El medico es obligatorio")
    @Positive(message = "El ID del medico debe ser mayor que cero")
    private Long idMedico;

    //La fecha debe ser hoy o una fecha futura
    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha no puede estar en el pasado")
    private LocalDate fecha;

    //Hora de inicio del horario
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;

    //Hora de finalización del horario
    @NotNull(message = "La hora de finalización es obligatoria")
    private LocalTime horaFin;


    public HorarioDisponibleRequestDTO() {
    }


    public Long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Long idMedico) {
        this.idMedico = idMedico;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public LocalTime getHoraFin() {
        return horaFin;
    }

    public void setHoraFin(LocalTime horaFin) {
        this.horaFin = horaFin;
    }
}