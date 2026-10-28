package com.proyectocitas.dto.response;

import java.time.LocalTime;
import java.time.LocalDate;

public class DoctorAgendaResponseDTO {

    private Long idCita;
    private String paciente;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalDate fecha;
    private String motivo;
    private String estado;

    public DoctorAgendaResponseDTO() {
    }

    public DoctorAgendaResponseDTO(
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
    
    public LocalDate getFecha() {
    return fecha;
    }

    public void setFecha(LocalDate fecha) {
    this.fecha = fecha;
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