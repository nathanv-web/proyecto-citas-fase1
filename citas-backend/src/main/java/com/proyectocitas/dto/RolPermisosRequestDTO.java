package com.proyectocitas.dto;

import com.proyectocitas.model.Permiso;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class RolPermisosRequestDTO {

    @NotNull(message = "La lista de permisos es obligatoria")
    private Set<Permiso> permisos;


    public RolPermisosRequestDTO() {
    }


    public Set<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }
}