package com.proyectocitas.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fotos_perfil")
public class FotoPerfil {
    @Id
    @Column(name = "id_usuario")
    private Long idUsuario;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "tipo_contenido", nullable = false, length = 32)
    private String tipoContenido;

    @Lob
    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] contenido;

    @Column(name = "actualizada_en", nullable = false)
    private LocalDateTime actualizadaEn;

    @PrePersist
    @PreUpdate
    void actualizarFecha() { actualizadaEn = LocalDateTime.now(); }

    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public String getTipoContenido() { return tipoContenido; }
    public void setTipoContenido(String tipoContenido) { this.tipoContenido = tipoContenido; }
    public byte[] getContenido() { return contenido; }
    public void setContenido(byte[] contenido) { this.contenido = contenido; }
}
