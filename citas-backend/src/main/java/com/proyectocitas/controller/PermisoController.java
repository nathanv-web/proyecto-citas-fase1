package com.proyectocitas.controller;

import com.proyectocitas.model.Permiso;

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
}