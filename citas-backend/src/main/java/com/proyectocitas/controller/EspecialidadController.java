package com.proyectocitas.controller;

import com.proyectocitas.exception.ResourceNotFoundException;

import com.proyectocitas.model.Especialidad;

import com.proyectocitas.service.EspecialidadService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;


    public EspecialidadController(
            EspecialidadService especialidadService
    ) {

        this.especialidadService =
                especialidadService;
    }


    // =================================================
    // LISTAR
    // =================================================

    @GetMapping
    public ResponseEntity<List<Especialidad>>
            obtenerTodas() {

        return ResponseEntity.ok(
                especialidadService.obtenerTodos()
        );
    }


    // =================================================
    // BUSCAR POR ID
    // =================================================

    @GetMapping("/{id}")
    public ResponseEntity<Especialidad>
            obtenerPorId(
                    @PathVariable Long id
            ) {

        Especialidad especialidad =
                especialidadService
                        .obtenerPorId(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No existe la especialidad con ID: "
                                                + id
                                )
                        );


        return ResponseEntity.ok(
                especialidad
        );
    }


    // =================================================
    // CREAR
    // =================================================

    @PostMapping
    public ResponseEntity<Especialidad>
            guardar(
                    @RequestBody Especialidad especialidad
            ) {

        Especialidad nueva =
                especialidadService
                        .guardar(especialidad);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(nueva);
    }


    // =================================================
    // ACTUALIZAR
    // =================================================

    @PutMapping("/{id}")
    public ResponseEntity<Especialidad>
            actualizar(
                    @PathVariable Long id,
                    @RequestBody Especialidad especialidad
            ) {

        Especialidad actualizada =
                especialidadService
                        .actualizar(
                                especialidad,
                                id
                        );


        return ResponseEntity.ok(
                actualizada
        );
    }


    // =================================================
    // ELIMINAR
    // =================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
            eliminar(
                    @PathVariable Long id
            ) {

        especialidadService
                .eliminar(id);


        return ResponseEntity
                .noContent()
                .build();
    }
}