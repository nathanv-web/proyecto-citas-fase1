package com.proyectocitas.dto;

import java.util.Set;

public class UsuarioDTO {

    //ID del usuario
    private Long idUsuario;
    //Nombre del usuario
    private String nombre;
    //Apellido del usuario
    private String apellido;
    //Correo del usuario
    private String correo;
    //Teléfono del usuario
    private String telefono;
    //Indica si el usuario está activo
    private Boolean activo;
    //Roles asignados al usuario
    private Set<String> roles;
    
    //Constructor , Getters y Setters

    public UsuarioDTO() {
    }


    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}