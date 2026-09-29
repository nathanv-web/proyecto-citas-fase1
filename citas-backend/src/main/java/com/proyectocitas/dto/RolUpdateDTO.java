package com.proyectocitas.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class RolUpdateDTO {

    @Size(
            max = 50,
            message = "El nombre del rol no puede superar los 50 caracteres"
    )
    @Pattern(
            regexp = "^ROLE_[A-Z][A-Z0-9_]*$",
            message = "El rol debe utilizar el formato ROLE_NOMBRE"
    )
    private String nombre;


    @Size(
            max = 255,
            message = "La descripción no puede superar los 255 caracteres"
    )
    private String descripcion;


    public RolUpdateDTO() {
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}