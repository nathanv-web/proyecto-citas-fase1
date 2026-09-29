package com.proyectocitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public class RolRequestDTO {
    
    @NotBlank(
    message = "El nombre del rol es obligatorio"
    )
    @Size(
    max = 50,
            message = "El nombre del rol no puede ser mayor a 50 caracteres"
    )
    @Pattern(
    regexp = "ROLE_[A-Z][A-Z0-9_]*$",
            message = "El rol debe de utilizar el formato ROLE_NOMBRE"
    )
    
    private String nombre;
    
    @Size(
    max = 255,
            message = "La descripción no se puede superar los 255 caracteres"
    )
    private String descripcion;
    
    public RolRequestDTO(){
        
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
