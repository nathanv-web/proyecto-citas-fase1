package com.proyectocitas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "estados_cita")
public class EstadoCita {
    
    //ATRIBUTOS

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Long idEstado;

    @Enumerated(EnumType.STRING)
    private NombreEstado nombre;

    private String descripcion;
    
    //CONSTRUCTOR


    public EstadoCita() {
    }
    
    //GETTERS Y SETTERS


    public Long getIdEstado() {
        return idEstado;
    }

    public void setIdEstado(Long idEstado) {
        this.idEstado = idEstado;
    }

    public NombreEstado getNombre() {
        return nombre;
    }

    public void setNombre(NombreEstado nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }


    public enum NombreEstado {
        PENDIENTE,
        COMPLETADA,
        CANCELADA
    }
}