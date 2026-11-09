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


import com.proyectocitas.dto.response.HorarioDisponibleResponseDTO;
import com.proyectocitas.dto.request.HorarioDisponibleCreateRequestDTO;
import com.proyectocitas.dto.request.HorarioDisponibleUpdateRequestDTO;

import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;

import com.proyectocitas.mapper.HorarioDisponibleMapper;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Medico;

import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;

import com.proyectocitas.service.ScheduleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Comparator;


@Tag(name = "Horarios", description = "Gestión de horarios del médico autenticado y consulta de disponibilidad.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MedicoRepository medicoRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final HorarioDisponibleMapper horarioMapper;
    private final CitaRepository citaRepository;


    public ScheduleController(
            ScheduleService scheduleService,
            MedicoRepository medicoRepository,
            HorarioDisponibleRepository horarioRepository,
            HorarioDisponibleMapper horarioMapper,
            CitaRepository citaRepository) {

        this.scheduleService = scheduleService;
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
        this.horarioMapper = horarioMapper;
        this.citaRepository = citaRepository;
    }


    // =================================================
    // CREAR HORARIO
    // =================================================

    @Operation(
        summary = "Crear horario de atención",
        description = "Crea un intervalo futuro para el médico autenticado. La hora de fin debe ser posterior a la de " +
            "inicio y el intervalo no puede superponerse con otros horarios del médico. Las fechas y horas " +
            "se interpretan en America/Guatemala. Requiere el permiso CREAR_HORARIOS.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Fecha y horas del intervalo. idMedico es obligatorio por validación, pero el propietario del " +
                "horario se determina mediante el usuario autenticado.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"idMedico\": 1, \"fecha\": \"2027-01-15\", \"horaInicio\": \"09:00:00\", \"horaFin\": \"09:30:00\"}"))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Registro creado correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = HorarioDisponibleResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping
    public ResponseEntity<HorarioDisponibleResponseDTO>
            crearHorario(
                    @Valid
                    @RequestBody
                    HorarioDisponibleCreateRequestDTO request,
                    @Parameter(hidden = true) Principal principal) {

        // ---------------------------------------------
        // BUSCAR MÉDICO AUTENTICADO
        // ---------------------------------------------

        Medico medico =
                medicoRepository
                        .findByUsuario_Correo(
                                principal.getName()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no tiene perfil de médico"
                                )
                        );


        HorarioDisponible guardado = scheduleService.crearHorarioAtomico(
                medico.getIdMedico(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin());


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        horarioMapper.toDTO(
                                guardado
                        )
                );
    }


    // =================================================
    // CONSULTAR HORARIOS DISPONIBLES
    // =================================================

    @Operation(
        summary = "Consultar horarios disponibles",
        description = "Devuelve los horarios disponibles de un médico activo para una fecha determinada, ordenados por " +
            "hora de inicio. Excluye los intervalos que ya comenzaron, según America/Guatemala. Requiere el " +
            "permiso VER_HORARIOS_DISPONIBLES.",
        parameters = {
            @Parameter(name = "idMedico", in = ParameterIn.QUERY,
                description = "Identificador del médico cuyos horarios se consultan.", example = "1", required = true),
            @Parameter(name = "fecha", in = ParameterIn.QUERY,
                description = "Fecha de consulta en formato AAAA-MM-DD.", example = "2027-01-15", required = true, schema = @Schema(type = "string", format = "date"))
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HorarioDisponibleResponseDTO.class)))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping("/available")
    public ResponseEntity<List<HorarioDisponibleResponseDTO>>
            obtenerHorariosDisponibles(
                    @RequestParam Long idMedico,
                    @RequestParam LocalDate fecha) {

        List<HorarioDisponibleResponseDTO> horarios =
                scheduleService
                        .consultarHorariosDisponibles(idMedico, fecha)
                        .stream()
                        .map(horarioMapper::toDTO)
                        .toList();


        return ResponseEntity.ok(
                horarios
        );
    }

    @Operation(
        summary = "Consultar mis próximos horarios",
        description = "Devuelve los horarios futuros del médico autenticado, ordenados por fecha y hora. Incluye los " +
            "distintos estados del horario. Requiere el permiso CREAR_HORARIOS.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = HorarioDisponibleResponseDTO.class)))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping("/my-schedules")
    public ResponseEntity<List<HorarioDisponibleResponseDTO>> obtenerMisHorarios(@Parameter(hidden = true) Principal principal) {
        Medico medico = medicoRepository.findByUsuario_Correo(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario autenticado no tiene perfil de médico"));

        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Guatemala"));
        return ResponseEntity.ok(horarioRepository.findByMedico_IdMedico(medico.getIdMedico())
                .stream()
                .filter(horario -> horario.getFecha() != null && horario.getHoraInicio() != null
                        && LocalDateTime.of(horario.getFecha(), horario.getHoraInicio()).isAfter(ahora))
                .sorted(Comparator.comparing(HorarioDisponible::getFecha)
                        .thenComparing(HorarioDisponible::getHoraInicio))
                .map(horarioMapper::toDTO)
                .toList());
    }


    // =================================================
    // ACTUALIZAR HORARIO
    // =================================================

    @Operation(
        summary = "Actualizar horario de atención",
        description = "Modifica un horario del médico autenticado. No permite modificar horarios pasados, ocupados, " +
            "asociados a citas o que se superpongan con otro intervalo. Requiere el permiso " +
            "ACTUALIZAR_HORARIOS.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador del horario.", example = "1", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Fecha y horas completas del nuevo intervalo, interpretadas en America/Guatemala.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"fecha\": \"2027-01-15\", \"horaInicio\": \"09:00:00\", \"horaFin\": \"09:30:00\"}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = HorarioDisponibleResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<HorarioDisponibleResponseDTO>
            actualizarHorario(
                    @PathVariable Long id,
                    @Valid
                    @RequestBody
                    HorarioDisponibleUpdateRequestDTO request,
                    @Parameter(hidden = true) Principal principal) {


        // ---------------------------------------------
        // BUSCAR MÉDICO AUTENTICADO
        // ---------------------------------------------

        Medico medico =
                medicoRepository
                        .findByUsuario_Correo(
                                principal.getName()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no tiene perfil de médico"
                                )
                        );


        HorarioDisponible actualizado = scheduleService.actualizarHorarioAtomico(
                id,
                medico.getIdMedico(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin());


        return ResponseEntity.ok(
                horarioMapper.toDTO(
                        actualizado
                )
        );
    }


    // =================================================
    // ELIMINAR HORARIO
    // =================================================

    @Operation(
        summary = "Eliminar horario de atención",
        description = "Elimina un horario del médico autenticado únicamente si no tiene una cita asociada. No permite " +
            "eliminar horarios de otro médico. Requiere el permiso ELIMINAR_HORARIOS.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador del horario.", example = "1", required = true)
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
            eliminarHorario(
                    @PathVariable Long id,
                    @Parameter(hidden = true) Principal principal) {


        // ---------------------------------------------
        // BUSCAR MÉDICO AUTENTICADO
        // ---------------------------------------------

        Medico medico =
                medicoRepository
                        .findByUsuario_Correo(
                                principal.getName()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no tiene perfil de médico"
                                )
                        );


        // ---------------------------------------------
        // BUSCAR HORARIO
        // ---------------------------------------------

        HorarioDisponible horario =
                horarioRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El horario indicado no existe"
                                )
                        );


        // ---------------------------------------------
        // VERIFICAR PROPIETARIO
        // ---------------------------------------------

        if (!horario
                .getMedico()
                .getIdMedico()
                .equals(
                        medico.getIdMedico()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes eliminar un horario de otro médico"
            );
        }


        // ---------------------------------------------
        // NO ELIMINAR SI YA TIENE CITA
        // ---------------------------------------------

        if (citaRepository
                .existsByHorario_IdHorario(id)) {

            throw new ScheduleConflictException(
                    "No se puede eliminar un horario que ya tiene una cita asociada"
            );
        }


        // ---------------------------------------------
        // ELIMINAR
        // ---------------------------------------------

        horarioRepository.delete(
                horario
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}
