package com.proyectocitas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(
    name = "notificaciones",
    indexes = {
        @Index(
            name = "idx_notif_destinatario_fecha",
            columnList = "id_destinatario, fecha_creacion"
        ),
        @Index(
            name = "idx_notif_destinatario_leida",
            columnList = "id_destinatario, leida"
        )
    }
)
public class Notificacion {

    public enum Tipo {
        CITA_RESERVADA,
        CITA_REPROGRAMADA,
        CITA_CANCELADA,
        DIAGNOSTICO_DISPONIBLE,
        RECORDATORIO_CITA,
        SISTEMA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_notificacion")
    private Long idNotificacion;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_destinatario", nullable = false)
    private Usuario destinatario;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private Tipo tipo;

    @Column(nullable = false)
    private boolean leida = false;

    @Column(
        name = "fecha_creacion",
        nullable = false,
        updatable = false
    )
    private LocalDateTime fechaCreacion;

    public Notificacion() {
    }

    @PrePersist
    public void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getIdNotificacion() {
        return idNotificacion;
    }

    public Usuario getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(Usuario destinatario) {
        this.destinatario = destinatario;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
