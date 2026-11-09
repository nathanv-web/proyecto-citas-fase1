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


import com.proyectocitas.dto.response.NotificacionResponseDTO;
import com.proyectocitas.service.NotificacionService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Notificaciones", description = "Consulta y seguimiento de notificaciones del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(
            NotificacionService notificacionService) {

        this.notificacionService = notificacionService;
    }

    // ==============================================
    // OBTENER MIS NOTIFICACIONES
    // ==============================================

    @Operation(
        summary = "Consultar mis notificaciones",
        description = "Devuelve las notificaciones que pertenecen al usuario autenticado. Requiere un JWT válido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NotificacionResponseDTO.class)))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping
    public ResponseEntity<List<NotificacionResponseDTO>>
            obtenerMisNotificaciones(@Parameter(hidden = true) Principal principal) {

        List<NotificacionResponseDTO> notificaciones =
                notificacionService.listarMias(
                        obtenerCorreo(principal)
                );

        return ResponseEntity.ok(notificaciones);
    }

    // ==============================================
    // CONTAR NOTIFICACIONES PENDIENTES
    // ==============================================

    @Operation(
        summary = "Contar mis notificaciones no leídas",
        description = "Devuelve el número de notificaciones pendientes de lectura en el campo noLeidas. Requiere un " +
            "JWT válido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object", additionalPropertiesSchema = Long.class),
                    examples = @ExampleObject(value = "{\"noLeidas\":2}"))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>>
            contarNoLeidas(@Parameter(hidden = true) Principal principal) {

        long cantidad = notificacionService.contarNoLeidas(
                obtenerCorreo(principal)
        );

        return ResponseEntity.ok(
                Map.of("noLeidas", cantidad)
        );
    }

    // ==============================================
    // MARCAR NOTIFICACION COMO LEIDA
    // ==============================================

    @Operation(
        summary = "Marcar notificación como leída",
        description = "Marca como leída una notificación del usuario autenticado y devuelve sus datos actualizados. " +
            "Requiere un JWT válido.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador de la notificación.", example = "1", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = NotificacionResponseDTO.class))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificacionResponseDTO>
            marcarComoLeida(
                    @PathVariable Long id,
                    @Parameter(hidden = true) Principal principal) {

        NotificacionResponseDTO respuesta =
                notificacionService.marcarComoLeida(
                        id,
                        obtenerCorreo(principal)
                );

        return ResponseEntity.ok(respuesta);
    }

    // ==============================================
    // VALIDAR USUARIO AUTENTICADO
    // ==============================================

    private String obtenerCorreo(@Parameter(hidden = true) Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        return principal.getName();
    }
}