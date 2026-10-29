package com.proyectocitas.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "email_verification_tokens", indexes = {
    @Index(name = "idx_email_verification_usuario", columnList = "id_usuario"),
    @Index(name = "idx_email_verification_expira", columnList = "expira_en")
})
public class EmailVerificationToken {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_token") private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false) private Usuario usuario;
    @Column(name = "token_hash", nullable = false, unique = true, length = 64) private String tokenHash;
    @Column(nullable = false, length = 255) private String correo;
    @Column(name = "creado_en", nullable = false) private Instant creadoEn;
    @Column(name = "expira_en", nullable = false) private Instant expiraEn;
    @Column(name = "usado_en") private Instant usadoEn;

    public Long getId() { return id; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario value) { usuario = value; }
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String value) { tokenHash = value; }
    public String getCorreo() { return correo; }
    public void setCorreo(String value) { correo = value; }
    public Instant getCreadoEn() { return creadoEn; }
    public void setCreadoEn(Instant value) { creadoEn = value; }
    public Instant getExpiraEn() { return expiraEn; }
    public void setExpiraEn(Instant value) { expiraEn = value; }
    public Instant getUsadoEn() { return usadoEn; }
    public void setUsadoEn(Instant value) { usadoEn = value; }
}
