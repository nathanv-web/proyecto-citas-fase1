package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.proyectocitas.dto.response.EstadisticaResponseDTO;
import com.proyectocitas.service.EstadisticaService;
import java.time.YearMonth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Estadísticas", description = "Resumen administrativo de usuarios, médicos, roles y citas del mes.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/admin/statistics")
public class EstadisticaController {
    private final EstadisticaService estadisticaService;

    public EstadisticaController(EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
    }

    @Operation(
        summary = "Consultar resumen estadístico administrativo",
        description = "Devuelve el total de usuarios, médicos y roles, y las citas del mes actual, junto con el mes " +
            "consultado. Requiere simultáneamente los permisos ACCESO_ADMIN, VER_USUARIOS, VER_MEDICOS, " +
            "VER_ROLES y VER_CITAS. Requiere un JWT válido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EstadisticaResponseDTO.class))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping
    public ResponseEntity<EstadisticaResponseDTO> obtenerResumen() {
        return ResponseEntity.ok(estadisticaService.obtenerResumen(YearMonth.now()));
    }
}
