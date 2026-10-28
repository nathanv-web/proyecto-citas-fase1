package com.proyectocitas.controller;

import com.proyectocitas.dto.response.EspecialidadResponseDTO;
import com.proyectocitas.dto.request.EspecialidadEstadoRequestDTO;
import com.proyectocitas.dto.request.EspecialidadRequestDTO;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.mapper.EspecialidadMapper;
import com.proyectocitas.model.Especialidad;
import com.proyectocitas.service.EspecialidadService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/especialidades")
public class EspecialidadController {

    private final EspecialidadService especialidadService;
    private final EspecialidadMapper especialidadMapper;


    public EspecialidadController( EspecialidadService especialidadService,
            EspecialidadMapper especialidadMapper
            ) {
        this.especialidadMapper = especialidadMapper;
        this.especialidadService = especialidadService;

    }


    // =================================================
    // LISTAR
    // =================================================

    @GetMapping
    public ResponseEntity<List<EspecialidadResponseDTO>>
            obtenerTodas() {

        List<EspecialidadResponseDTO> especialidades =
                especialidadService
                        .obtenerTodos()
                        .stream()
                        .map(especialidadMapper::toDTO)
                        .toList();

        return ResponseEntity.ok(
                especialidades
        );
    }


    // =================================================
    // BUSCAR POR ID
    // =================================================

    @GetMapping("/{id}")
    public ResponseEntity<EspecialidadResponseDTO>
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
                especialidadMapper.toDTO(
                        especialidad
                )
        );
    }


    // =================================================
    // CREAR
    // =================================================

    @PostMapping
    public ResponseEntity<EspecialidadResponseDTO>
            guardar(
                    @Valid
                    @RequestBody
                    EspecialidadRequestDTO request
            ) {

        Especialidad especialidad =
                especialidadMapper
                        .toEntity(request);

        Especialidad nueva =
                especialidadService
                        .guardar(especialidad);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        especialidadMapper
                                .toDTO(nueva)
                );
    }


    // =================================================
    // ACTUALIZAR
    // =================================================

    @PutMapping("/{id}")
    public ResponseEntity<EspecialidadResponseDTO>
            actualizar(
                    @PathVariable Long id,

                    @Valid
                    @RequestBody
                    EspecialidadRequestDTO request
            ) {

        Especialidad especialidad =
                especialidadMapper
                        .toEntity(request);

        Especialidad actualizada =
                especialidadService
                        .actualizar(
                                especialidad,
                                id
                        );

        return ResponseEntity.ok(
                especialidadMapper
                        .toDTO(actualizada)
        );
    }


    // =================================================
    // CAMBIAR SOLO EL ESTADO, CONSERVANDO LOS DEMÁS DATOS
    // =================================================

    @PatchMapping("/{id}/estado")
    public ResponseEntity<EspecialidadResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody EspecialidadEstadoRequestDTO request
    ) {
        Especialidad cambios = new Especialidad();
        cambios.setActivo(request.activo());
        Especialidad actualizada = especialidadService.actualizar(cambios, id);
        return ResponseEntity.ok(especialidadMapper.toDTO(actualizada));
    }

    // =================================================
    // DESACTIVAR (NO BORRAR)
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
