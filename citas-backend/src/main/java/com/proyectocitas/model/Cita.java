package com.proyectocitas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String pacienteNombre;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private String estado; // Ejemplo: "PENDIENTE", "CONFIRMADA", "CANCELADA"

    @ManyToOne
    @JoinColumn(name = "medico_id", nullable = false)
    private Medico medico;

    public Cita() {}

    public Cita(String pacienteNombre, LocalDateTime fechaHora, String estado, Medico medico) {
        this.pacienteNombre = pacienteNombre;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.medico = medico;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPacienteNombre() { return pacienteNombre; }
    public void setPacienteNombre(String pacienteNombre) { this.pacienteNombre = pacienteNombre; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Medico getMedico() { return medico; }
    public void setMedico(Medico medico) { this.medico = medico; }
}