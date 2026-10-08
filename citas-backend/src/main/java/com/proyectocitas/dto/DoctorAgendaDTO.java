package com.proyectocitas.dto;

import java.time.LocalTime;

public class DoctorAgendaDTO {

    private Long idCita;
    private String paciente;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String motivo;
    private String estado;

    public DoctorAgendaDTO() {
    }

    public DoctorAgendaDTO(
            Long idCita,
            String paciente,
            LocalTime horaInicio,
            LocalTime horaFin,
            String motivo,
            String estado) {

        this.idCita = idCita;
        this.paciente = paciente;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.motivo = motivo;
        this.estado = estado;
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

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}