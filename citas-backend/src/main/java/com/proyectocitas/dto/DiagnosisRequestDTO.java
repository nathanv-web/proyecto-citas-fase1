package com.proyectocitas.dto;

import jakarta.validation.constraints.NotBlank;

public class DiagnosisRequestDTO {

    @NotBlank(message = "El diagnóstico y la receta son obligatorios")
    private String diagnosticoReceta;

    private String observaciones;

    public DiagnosisRequestDTO() {
    }

    public String getDiagnosticoReceta() {
        return diagnosticoReceta;
    }

    public void setDiagnosticoReceta(String diagnosticoReceta) {
        this.diagnosticoReceta = diagnosticoReceta;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}