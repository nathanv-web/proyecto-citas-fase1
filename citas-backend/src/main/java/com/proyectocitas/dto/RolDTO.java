package com.proyectocitas.dto;

import java.util.HashSet;
import java.util.Set;

public class RolDTO {

    private Long idRol;
    private String nombre;
    private String descripcion;

    private Set<String> permisos =
            new HashSet<>();


    public RolDTO() {
    }


    public Long getIdRol() {
        return idRol;
    }

    public void setIdRol(Long idRol) {
        this.idRol = idRol;
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


    public Set<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<String> permisos) {
        this.permisos = permisos;
    }
}