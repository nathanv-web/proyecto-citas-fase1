package com.proyectocitas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateDTO {

    @Size(
        min = 2,
        max = 100,
        message = "El nombre debe tener entre 2 y 100 caracteres"
    )
    private String nombre;

    @Size(
        min = 2,
        max = 100,
        message = "El apellido debe tener entre 2 y 100 caracteres"
    )
    private String apellido;

    @Email(
        message = "El correo debe tener un formato válido"
    )
    private String correo;

    @Size(
        max = 20,
        message = "El teléfono no puede superar los 20 caracteres"
    )
    private String telefono;

    private Boolean activo;


    public UsuarioUpdateDTO() {
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
}