package com.proyectocitas.dto.response;

import java.util.Set;


public class LoginUsuarioResponseDTO {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String correo;
    private boolean correoVerificado;

    private Set<String> roles;
    private Set<String> permisos;


    // =================================================
    // CONSTRUCTOR
    // =================================================

    public LoginUsuarioResponseDTO(
            Long idUsuario,
            String nombre,
            String apellido,
            String correo,
            Set<String> roles,
            Set<String> permisos) {

        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.correo = correo;
        this.roles = roles;
        this.permisos = permisos;
    }


    // =================================================
    // GETTERS
    // =================================================

    public Long getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public boolean isCorreoVerificado() { return correoVerificado; }
    public void setCorreoVerificado(boolean value) { correoVerificado = value; }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermisos() {
        return permisos;
    }
}
