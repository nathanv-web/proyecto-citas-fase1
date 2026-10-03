package com.proyectocitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public class MedicoRequestDTO {

    //ID del usuario que tendrá el perfil de   medico
    @NotNull(message = "El usuario es obligatorio")
    @Positive(message = "El ID del usuario debe ser mayor que cero")
    private Long idUsuario;

    //ID de la especialidad del medico
    @NotNull(message = "La especialidad es obligatoria")
    @Positive(message = "El ID de la especialidad debe ser mayor que cero")
    private Long idEspecialidad;

    //Número de colegiado del medico
    @NotBlank(message = "El número de colegiado es obligatorio")
    @Size(
        max = 50,
        message = "El colegiado no puede superar los 50 caracteres"
    )
    private String colegiado;

    //Años de experiencia profesional
    @PositiveOrZero(message = "Los años de experiencia no pueden ser negativos")
    private Integer aniosExperiencia;

    //Descripción o información profesional del medico
    @Size(
        max = 1000,
        message = "La biografía no puede superar los 1000 caracteres"
    )
    private String biografia;


    public MedicoRequestDTO() {
    }


    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Long getIdEspecialidad() {
        return idEspecialidad;
    }

    public void setIdEspecialidad(Long idEspecialidad) {
        this.idEspecialidad = idEspecialidad;
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
}