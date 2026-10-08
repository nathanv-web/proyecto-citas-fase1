package com.proyectocitas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateDTO {

    @Size(
        min = 2,
        max = 100,
        message = "El nombre debe tener entre 2 y 100 caracteres"
    )
        @Pattern(
            regexp = "^[\\p{L} .'-]+$",
            message = "El nombre solo puede contener letras"
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
        @Pattern(
            regexp = "^[\\p{L} .'-]+$",
            message = "El apellido solo puede contener letras"
    )
    private String correo;

    @Size(
        max = 20,
        message = "El teléfono no puede superar los 20 caracteres"
    )
        @Pattern(
            regexp = "^[0-9]{8,15}$",
            message = "El teléfono solo puede contener números y debe tener entre 8 y 15 dígitos"
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