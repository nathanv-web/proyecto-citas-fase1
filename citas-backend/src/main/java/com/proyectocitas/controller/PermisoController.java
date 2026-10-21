package com.proyectocitas.controller;

import com.proyectocitas.model.Permiso;
import com.proyectocitas.dto.PermisoMetadataDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
public class PermisoController {


    // =================================================
    // LISTAR PERMISOS DISPONIBLES
    // =================================================

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