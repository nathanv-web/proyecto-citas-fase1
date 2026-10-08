package com.proyectocitas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DiagnosisRequestDTO {

    @NotBlank(message = "El diagnóstico y la receta son obligatorios")
    @Size(min = 5, max = 5000,
            message = "El diagnostico debe tener entre 5 y 5000 caracteres")
    private String diagnosticoReceta;
    
    @Size(max = 2000,
            message = "Las observacionesn no puden superar los 2000 caracteres")
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