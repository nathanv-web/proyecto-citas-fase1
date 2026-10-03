package com.proyectocitas.dto;

import java.util.Set;


public class LoginUsuarioDTO {

    private Long idUsuario;
    private String nombre;
    private String apellido;
    private String correo;

    private Set<String> roles;
    private Set<String> permisos;


    // =================================================
    // CONSTRUCTOR
    // =================================================

    public LoginUsuarioDTO(
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

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getPermisos() {
        return permisos;
    }
}