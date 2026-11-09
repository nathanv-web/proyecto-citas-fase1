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


import com.proyectocitas.dto.request.AppointmentCreateRequestDTO;
import com.proyectocitas.dto.response.AppointmentResponseDTO;
import com.proyectocitas.dto.request.DiagnosisRequestDTO;
import com.proyectocitas.dto.response.DoctorAgendaResponseDTO;
import com.proyectocitas.service.AppointmentService;

import jakarta.validation.Valid;

import com.proyectocitas.dto.request.CancelAppointmentRequestDTO;
import com.proyectocitas.dto.request.RescheduleRequestDTO;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatusCode;


@Tag(name = "Citas médicas", description = "Reserva, historial, agenda, diagnóstico, cancelación y reprogramación de citas.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;


    public AppointmentController(
            AppointmentService appointmentService) {

        this.appointmentService = appointmentService;
    }


    // Crear una nueva cita médica
    @Operation(
        summary = "Reservar cita médica",
        description = "Reserva un horario futuro y disponible para el paciente autenticado. Crea la cita en estado " +
            "PENDIENTE; el horario debe pertenecer a un médico activo. Requiere el permiso CREAR_CITA.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Identificador de un horario disponible y motivo de consulta de 5 a 500 caracteres.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"idHorario\": 1, \"motivo\": \"Consulta médica general\"}"))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Registro creado correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> crearCita(
            @Valid @RequestBody AppointmentCreateRequestDTO request,
            @Parameter(hidden = true) Principal principal) {

        if (principal == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        AppointmentResponseDTO cita =
                appointmentService.agendarCita(
                        request,
                        principal.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cita);
    }


    // Registrar diagnóstico y completar cita
    @Operation(
        summary = "Registrar diagnóstico de una cita",
        description = "Registra el diagnóstico y la receta, agrega observaciones opcionales y completa una cita " +
            "PENDIENTE. Solo el médico asignado puede realizar la operación. Requiere el permiso " +
            "REGISTRAR_DIAGNOSTICO.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador de la cita médica.", example = "1", required = true)
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Diagnóstico y receta de 5 a 5000 caracteres y observaciones opcionales de hasta 2000.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"diagnosticoReceta\": \"Evaluación clínica y tratamiento indicado durante la consulta.\", \"observaciones\": \"Seguimiento según indicación médica.\"}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PutMapping("/{id}/diagnosis")
    public ResponseEntity<AppointmentResponseDTO>
            registrarDiagnostico(
                    @PathVariable("id") Long idCita,
                    @Valid
                    @RequestBody
                    DiagnosisRequestDTO diagnosisRequest,
                    @Parameter(hidden = true) Principal principal) {

        if (principal == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        AppointmentResponseDTO citaActualizada =
                appointmentService.registrarDiagnostico(
                        idCita,
                        diagnosisRequest,
                        principal.getName()
                );

        return ResponseEntity.ok(citaActualizada);
    }


    // Historial del paciente autenticado
    @Operation(
        summary = "Consultar mi historial de citas",
        description = "Devuelve el historial de citas del paciente autenticado. Requiere el permiso VER_HISTORIAL.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = AppointmentResponseDTO.class)))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping("/my-history")
    public ResponseEntity<List<AppointmentResponseDTO>>
            obtenerMiHistorial(
                    @Parameter(hidden = true) Principal principal) {

        if (principal == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        List<AppointmentResponseDTO> historial =
                appointmentService.obtenerMiHistorial(
                        principal.getName()
                );

        return ResponseEntity.ok(historial);
    }

// =================================================
// CONSULTAR AGENDA DEL MEDICO CON FILTROS
// =================================================

@Operation(
    summary = "Consultar mi agenda médica",
    description = "Devuelve las citas del médico autenticado. Se puede consultar por fecha y periodo, o por el " +
        "rango desde/hasta; ambos modos no se pueden combinar. Si se omiten fecha y periodo, utiliza el " +
        "día actual. Requiere uno de los permisos: VER_CITAS o REGISTRAR_DIAGNOSTICO.",
    parameters = {
        @Parameter(name = "fecha", in = ParameterIn.QUERY,
            description = "Fecha de referencia para el periodo; por defecto se utiliza la fecha actual. No combinar con desde/hasta.", example = "2027-01-15", schema = @Schema(type = "string", format = "date")),
        @Parameter(name = "periodo", in = ParameterIn.QUERY,
            description = "Periodo respecto a fecha; por defecto dia. No combinar con desde/hasta.", example = "dia",
            schema = @Schema(allowableValues = {"dia", "semana", "mes"})),
        @Parameter(name = "desde", in = ParameterIn.QUERY,
            description = "Fecha inicial del rango. Requiere hasta y excluye fecha/periodo.", example = "2027-01-01", schema = @Schema(type = "string", format = "date")),
        @Parameter(name = "hasta", in = ParameterIn.QUERY,
            description = "Fecha final del rango, igual o posterior a desde. Requiere desde.", example = "2027-01-31", schema = @Schema(type = "string", format = "date"))
    },
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = DoctorAgendaResponseDTO.class)))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@GetMapping("/my-agenda")
public ResponseEntity<List<DoctorAgendaResponseDTO>> obtenerMiAgenda(

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fecha,

        @RequestParam(required = false)
        String periodo,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate desde,

        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate hasta,

        @Parameter(hidden = true) Principal principal) {

    if (principal == null) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no autenticado"
        );
    }

    List<DoctorAgendaResponseDTO> agenda =
            appointmentService.obtenerMiAgenda(
                    principal.getName(),
                    fecha,
                    periodo,
                    desde,
                    hasta
            );

    return ResponseEntity.ok(agenda);
}

// =================================================
// CANCELAR CITA CON MOTIVO
// =================================================

@Operation(
    summary = "Cancelar cita médica",
    description = "Cancela una cita PENDIENTE del paciente autenticado y libera el horario. El cuerpo es opcional " +
        "y permite registrar un motivo de cancelación. Requiere el permiso CANCELAR_CITA.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador de la cita médica.", example = "1", required = true)
    },
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Motivo opcional de cancelación de hasta 500 caracteres. La solicitud también se acepta sin " +
            "cuerpo.",
        required = false, useParameterTypeSchema = true,
        content = @Content(mediaType = "application/json", examples = @ExampleObject(
            name = "Ejemplo", value = "{\"motivoCancelacion\": \"No podré asistir en la fecha programada.\"}"))),
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponseDTO.class))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@PutMapping("/{id}/cancel")
public ResponseEntity<AppointmentResponseDTO> cancelarCita(
        @PathVariable Long id,
        @Valid @RequestBody(required = false)
        CancelAppointmentRequestDTO request,
        @Parameter(hidden = true) Principal principal) {

    if (principal == null) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no autenticado"
        );
    }

    String motivoCancelacion = request == null
            ? null
            : request.getMotivoCancelacion();

    AppointmentResponseDTO respuesta =
            appointmentService.cancelarCita(
                    id,
                    principal.getName(),
                    motivoCancelacion
            );

    return ResponseEntity.ok(respuesta);
}
// =================================================
// REPROGRAMAR CITA
// =================================================

@Operation(
    summary = "Reprogramar cita médica",
    description = "Cambia una cita PENDIENTE del paciente autenticado a un horario diferente, futuro y disponible " +
        "del mismo médico; libera el horario anterior. Requiere el permiso CREAR_CITA.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador de la cita médica.", example = "1", required = true)
    },
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Identificador del nuevo horario disponible, distinto del horario actual.",
        required = true, useParameterTypeSchema = true,
        content = @Content(mediaType = "application/json", examples = @ExampleObject(
            name = "Ejemplo", value = "{\"idHorarioNuevo\": 2}"))),
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AppointmentResponseDTO.class))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@PutMapping("/{id}/reschedule")
public ResponseEntity<AppointmentResponseDTO> reprogramarCita(
        @PathVariable("id") Long idCita,
        @Valid @RequestBody RescheduleRequestDTO request,
        @Parameter(hidden = true) Principal principal) {

    if (principal == null) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no autenticado"
        );
    }

    AppointmentResponseDTO respuesta =
            appointmentService.reprogramarCita(
                    idCita,
                    request,
                    principal.getName()
            );

    return ResponseEntity.ok(respuesta);
}

}
