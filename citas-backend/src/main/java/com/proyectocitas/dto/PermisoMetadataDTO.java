package com.proyectocitas.dto;

public class PermisoMetadataDTO {

    private String nombre;
    private String categoria;

    public PermisoMetadataDTO() {
    }

    public PermisoMetadataDTO(
            String nombre,
            String categoria) {

        this.nombre = nombre;
        this.categoria = categoria;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
}