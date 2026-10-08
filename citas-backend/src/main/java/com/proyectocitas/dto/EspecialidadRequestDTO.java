package com.proyectocitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EspecialidadRequestDTO {

    @NotBlank(message = "El nombre de la especialidad es obligatorio")
    @Size(
            min = 2,
            max = 100,
            message = "El nombre debe contener entre 2 y 100 caracteres"
    )
    @Pattern(
            regexp = "^[\\p{L} .'-]+$",
            message = "El nombre de la especialidad solo puede contener letras"
    )
    private String nombre;

    @Size(
            max = 255,
            message = "La descripción no puede superar los 255 caracteres"
    )
    private String descripcion;

    private Boolean activo;

    public EspecialidadRequestDTO() {
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

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}