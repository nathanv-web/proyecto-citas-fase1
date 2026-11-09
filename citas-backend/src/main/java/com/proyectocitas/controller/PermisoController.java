package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.proyectocitas.model.Permiso;
import com.proyectocitas.dto.PermisoMetadataDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Permisos", description = "Catálogo de permisos disponibles y sus categorías funcionales.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/permissions")
public class PermisoController {


    // =================================================
    // LISTAR PERMISOS DISPONIBLES
    // =================================================

    @Operation(
        summary = "Listar permisos disponibles",
        description = "Devuelve los nombres de todos los permisos definidos por el sistema. Requiere el permiso " +
            "ASIGNAR_PERMISOS.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = String.class)))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping
    public ResponseEntity<List<String>> listarPermisos() {

        List<String> permisos =
                Arrays.stream(Permiso.values())
                        .map(Enum::name)
                        .toList();

        return ResponseEntity.ok(permisos);
    }

    // =================================================
// LISTAR METADATA DE PERMISOS
// =================================================
// Devuelve el nombre real del permiso y una categoría
// únicamente para facilitar su organización visual.
//
// El nombre persistido del enum NO se modifica.
@Operation(
    summary = "Consultar permisos y sus categorías",
    description = "Devuelve cada permiso junto con su categoría funcional. Esta ruta requiere autenticación y no " +
        "tiene un permiso adicional configurado. Requiere un JWT válido.",
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = PermisoMetadataDTO.class)))),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@GetMapping("/metadata")
public ResponseEntity<List<PermisoMetadataDTO>> listarMetadataPermisos() {

    List<PermisoMetadataDTO> permisos =
            Arrays.stream(Permiso.values())
                    .map(permiso ->
                            new PermisoMetadataDTO(
                                    permiso.name(),
                                    obtenerCategoria(permiso)
                            )
                    )
                    .toList();

    return ResponseEntity.ok(permisos);
}


// =================================================
// CATEGORIZAR PERMISOS
// =================================================
private String obtenerCategoria(Permiso permiso) {

    String nombre =
            permiso.name();

    if (nombre.startsWith("ACCESO_")) {
        return "ACCESO";
    }

    if (nombre.contains("USUARIOS")) {
        return "USUARIOS";
    }

    if (nombre.contains("ROLES")
            || nombre.contains("PERMISOS")) {
        return "ROLES_Y_PERMISOS";
    }

    if (nombre.contains("MEDICOS")) {
        return "MEDICOS";
    }

    if (nombre.contains("ESPECIALIDADES")) {
        return "ESPECIALIDADES";
    }

    if (nombre.contains("HORARIOS")) {
        return "HORARIOS";
    }

    if (nombre.contains("CITA")
            || nombre.contains("CITAS")
            || nombre.contains("HISTORIAL")
            || nombre.contains("DIAGNOSTICO")) {
        return "CITAS";
    }

    return "OTROS";
}
}