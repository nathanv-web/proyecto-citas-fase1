package com.proyectocitas.dto;

public class EstadoCitaDTO {

    //ID del estado de la cita
    private Long idEstado;

    //Nombre del estado
    private String nombre;

    //Descripción del estado
    private String descripcion;
    
    //Constructor, Getters y Setters
    public EstadoCitaDTO() {
    }


    public Long getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(Long idEstado) {
        this.idEstado = idEstado;
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