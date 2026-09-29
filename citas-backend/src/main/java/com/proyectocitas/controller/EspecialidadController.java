package com.proyectocitas.controller;

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
            EspecialidadService especialidadService) {

        this.especialidadService = especialidadService;
    }

    @GetMapping
    public List<Especialidad> obtenerTodas() {
        return especialidadService.obtenerTodos();
    }

    @PostMapping
    public ResponseEntity<Especialidad> crear(
            @RequestBody Especialidad especialidad) {

        Especialidad guardada =
                especialidadService.guardar(especialidad);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(guardada);
    }
}