package com.proyectocitas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas_medicas")
public class Cita {
    
    //ATRIBUTOS

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cita")
    private Long idCita;
    
    //RELACIONES 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_horario", nullable = false)
    private HorarioDisponible horario;

    @ManyToOne
    @JoinColumn(name = "id_paciente")
    private Usuario paciente;

    @ManyToOne
    @JoinColumn(name = "id_estado")
    private EstadoCita estado;

    // ATRIBUTOS
    
    private String motivo;
    @Column(name = "motivo_cancelacion", length = 500)
    private String motivoCancelacion;

    @Column(name = "diagnostico_receta", columnDefinition = "TEXT")
    private String diagnosticoReceta;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;
    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "recordatorio_enviado", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean recordatorioEnviado;
    
    //CONSTRUCTOR


    public Cita() {
    }
    
    // Antes de insertar la cita en la base de datos, si no tiene fecha de creación, coloca la fecha y hora actual.

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
    
    //GETTERS Y SETTERS


    public Long getIdCita() {
        return idCita;
    }

    public void setIdCita(Long idCita) {
        this.idCita = idCita;
    }

    public HorarioDisponible getHorario() {
        return horario;
    }

    public void setHorario(HorarioDisponible horario) {
        this.horario = horario;
    }

    public Usuario getPaciente() {
        return paciente;
    }

    public void setPaciente(Usuario paciente) {
        this.paciente = paciente;
    }

    public EstadoCita getEstado() {
        return estado;
    }

    public void setEstado(EstadoCita estado) {
        this.estado = estado;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
    public String getMotivoCancelacion() {
    return motivoCancelacion;
    }

    public void setMotivoCancelacion(String motivoCancelacion) {
    this.motivoCancelacion = motivoCancelacion;
    }

    public String getDiagnosticoReceta() {
        return diagnosticoReceta;
    }

    public void setDiagnosticoReceta(String diagnosticoReceta) {
        this.diagnosticoReceta = diagnosticoReceta;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public boolean isRecordatorioEnviado() {
        return recordatorioEnviado;
    }

    public void setRecordatorioEnviado(boolean recordatorioEnviado) {
        this.recordatorioEnviado = recordatorioEnviado;
    }
}
