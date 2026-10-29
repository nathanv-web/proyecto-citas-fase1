package com.proyectocitas.dto.response;

/** Resumen administrativo sin información personal ni valores de demostración. */
public class EstadisticaResponseDTO {
    private final long totalUsuarios;
    private final long totalMedicos;
    private final long totalCitasMes;
    private final long totalRoles;
    private final String mes;

    public EstadisticaResponseDTO(long totalUsuarios, long totalMedicos, long totalCitasMes,
            long totalRoles, String mes) {
        this.totalUsuarios = totalUsuarios;
        this.totalMedicos = totalMedicos;
        this.totalCitasMes = totalCitasMes;
        this.totalRoles = totalRoles;
        this.mes = mes;
    }

    public long getTotalUsuarios() { return totalUsuarios; }
    public long getTotalMedicos() { return totalMedicos; }
    public long getTotalCitasMes() { return totalCitasMes; }
    public long getTotalRoles() { return totalRoles; }
    public String getMes() { return mes; }
}
