package com.proyectocitas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "medicos")
public class Medico {

    //ATRIBUTOS

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_medico")
    private Long idMedico;


    //RELACION CON TABLA USUARIO
    @OneToOne
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;


    //RELACION CON TABLA ESPECIALIDAD
    @ManyToOne
    @JoinColumn(name = "id_especialidad")
    private Especialidad especialidad;


    //ATRIBUTOS

    private String colegiado;

    @Column(name = "anios_experiencia")
    private Integer aniosExperiencia;

    private String biografia;

    //Estado actual del medico
    @Enumerated(EnumType.STRING)
    private EstadoMedico estado = EstadoMedico.ACTIVO;


    //CONSTRUCTOR

    public Medico() {
    }


    //GETTERS Y SETTERS

    public Long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Long idMedico) {
        this.idMedico = idMedico;
    }


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }


    public Especialidad getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(Especialidad especialidad) {
        this.especialidad = especialidad;
    }


    public String getColegiado() {
        return colegiado;
    }

    public void setColegiado(String colegiado) {
        this.colegiado = colegiado;
    }


    public Integer getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(Integer aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }


    public String getBiografia() {
        return biografia;
    }

    public void setBiografia(String biografia) {
        this.biografia = biografia;
    }


    public EstadoMedico getEstado() {
        return estado;
    }

    public void setEstado(EstadoMedico estado) {
        this.estado = estado;
    }


    //ESTADOS POSIBLES DEL MEDICO
    public enum EstadoMedico {
        ACTIVO,
        INACTIVO
    }
}