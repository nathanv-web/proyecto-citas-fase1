package com.proyectocitas.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.Set;


public class UsuarioRolesRequestDTO {

    @NotEmpty(
            message = "Debe seleccionar al menos un rol"
    )
    private Set<Long> idRol;


    public UsuarioRolesRequestDTO() {
    }


    public Set<Long> getIdRol() {
        return idRol;
    }

    public void setIdRol(Set<Long> idRol) {
        this.idRol = idRol;
    }
}