package com.proyectocitas.dto;

public class MedicoDTO {

    //ID del medico
    private Long idMedico;

    //Nombre completo del medico
    private String nombre;

    //Correo del usuario asociado al medico
    private String correo;

    //Nombre de la especialidad del medico
    private String especialidad;

    //Número de colegiado del medico
    private String colegiado;

    //Años de experiencia profesional
    private Integer aniosExperiencia;

    //Información profesional del medico
    private String biografia;

    //Estado actual del medico
    private String estado;


    public MedicoDTO() {
    }


    public Long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(Long idMedico) {
        this.idMedico = idMedico;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(String especialidad) {
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}