package com.proyectocitas.model; 

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
public class Rol {

    // =================================================
    // ATRIBUTOS
    // =================================================

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Long idRol;

    @Column(
            nullable = false,
            unique = true
    )
    private String nombre;

    @Column(length = 255)
    private String descripcion;


    // =================================================
    // PERMISOS DEL ROL
    // =================================================

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(
            name = "rol_permiso",
            joinColumns = @JoinColumn(name = "id_rol")
    )
    @Column(
            name = "permiso",
            nullable = false
    )
    private Set<Permiso> permisos = new HashSet<>();


    // =================================================
    // CONSTRUCTORES
    // =================================================

    public Rol() {
    }

    public Rol(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }


    // =================================================
    // GETTERS Y SETTERS
    // =================================================

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

    public Set<Permiso> getPermisos() {
        return permisos;
    }

    public void setPermisos(Set<Permiso> permisos) {
        this.permisos = permisos;
    }
}