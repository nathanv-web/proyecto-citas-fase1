package com.proyectocitas.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentResponseDTO {

    //ID de la cita médica
    private Long idCita;

    //Nombre completo del paciente
    private String paciente;

    //Nombre completo del medico
    private String medico;

    //Especialidad del medico
    private String especialidad;

    //Fecha de la cita
    private LocalDate fecha;

    //Hora de inicio de la cita
    private LocalTime horaInicio;

    //Hora de finalización de la cita
    private LocalTime horaFin;

    //Estado actual de la cita
    private String estado;

    //Motivo de la consulta
    private String motivo;

    //Diagnóstico y receta de la cita
    private String diagnosticoReceta;

    //Observaciones adicionales
    private String observaciones;


    public AppointmentResponseDTO() {
    }


    public Long getIdCita() {
        return idCita;
    }

    public void setIdCita(Long idCita) {
        this.idCita = idCita;
    }

    public String getPaciente() {
        return paciente;
    }

    public void setPaciente(String paciente) {
        this.paciente = paciente;
    }

    public String getMedico() {
        return medico;
    }

    public void setMedico(String medico) {
        this.medico = medico;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
        this.especialidad = especialidad;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getDiagnosticoReceta() {
        return diagnosticoReceta;
    }

    public void setDiagnosticoReceta(String diagnosticoReceta) {
        this.diagnosticoReceta = diagnosticoReceta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}