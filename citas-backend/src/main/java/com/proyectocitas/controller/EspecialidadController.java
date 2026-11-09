package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


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

@Tag(name = "Especialidades", description = "Catálogo de especialidades médicas y administración de su estado.")
@SecurityRequirement(name = "bearerAuth")
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

    @Operation(
        summary = "Listar especialidades",
        description = "Devuelve las especialidades registradas con su descripción y estado. Requiere el permiso " +
            "VER_ESPECIALIDADES.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = EspecialidadResponseDTO.class)))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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

    @Operation(
        summary = "Consultar especialidad por ID",
        description = "Obtiene los datos de una especialidad por su identificador. Requiere el permiso " +
            "VER_ESPECIALIDADES.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador de la especialidad.", example = "1", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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

    @Operation(
        summary = "Crear especialidad",
        description = "Registra una especialidad con nombre único, descripción y estado. Requiere el permiso " +
            "CREAR_ESPECIALIDADES.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nombre de 2 a 100 caracteres, descripción opcional de hasta 255 y estado activo.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"nombre\": \"Cardiología\", \"descripcion\": \"Atención de enfermedades cardiovasculares\", \"activo\": true}"))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Registro creado correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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

    @Operation(
        summary = "Actualizar especialidad",
        description = "Actualiza los datos de una especialidad. No permite desactivarla si tiene médicos activos " +
            "asociados. Requiere el permiso ACTUALIZAR_ESPECIALIDADES.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador de la especialidad.", example = "1", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nombre obligatorio de la especialidad y los datos que se desean actualizar.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"nombre\": \"Cardiología\", \"descripcion\": \"Atención de enfermedades cardiovasculares\", \"activo\": true}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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

    @Operation(
        summary = "Cambiar estado de una especialidad",
        description = "Activa o desactiva una especialidad. Para desactivarla no debe tener médicos activos asociados. " +
            "Requiere el permiso ACTUALIZAR_ESPECIALIDADES.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador de la especialidad.", example = "1", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Estado deseado de la especialidad: true para activa y false para inactiva.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"activo\": false}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = EspecialidadResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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

    @Operation(
        summary = "Desactivar especialidad",
        description = "Marca la especialidad como inactiva sin borrarla. Rechaza la operación si tiene médicos activos " +
            "asociados. Requiere el permiso ELIMINAR_ESPECIALIDADES.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador de la especialidad.", example = "1", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "Operación realizada correctamente, sin contenido de respuesta.", content = @Content),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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
