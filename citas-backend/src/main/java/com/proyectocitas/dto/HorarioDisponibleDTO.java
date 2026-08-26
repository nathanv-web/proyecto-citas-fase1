package com.proyectocitas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class HorarioDisponibleDTO {

    //ID del horario disponible
    private Long idHorario;

    //ID del medico al que pertenece el horario
    private Long idMedico;

    //Nombre completo del medico
    private String medico;

    //Fecha disponible para la cita
    private LocalDate fecha;

    //Hora de inicio del horario
    private LocalTime horaInicio;

    //Hora de finalización del horario
    private LocalTime horaFin;

    //Estado actual del horario
    private String estado;


    //Constructor

    public HorarioDisponibleDTO() {
    }


    //Getters y Setters

    public Long getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(Long idHorario) {
        this.idHorario = idHorario;
    }

    public Long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Long idMedico) {
        this.idMedico = idMedico;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}