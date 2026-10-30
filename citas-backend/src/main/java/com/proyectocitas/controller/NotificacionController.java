package com.proyectocitas.controller;

import com.proyectocitas.dto.response.NotificacionResponseDTO;
import com.proyectocitas.service.NotificacionService;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping
    public ResponseEntity<List<NotificacionResponseDTO>>
            obtenerMisNotificaciones(Principal principal) {

        List<NotificacionResponseDTO> notificaciones =
                notificacionService.listarMias(
                        obtenerCorreo(principal)
                );

        return ResponseEntity.ok(notificaciones);
    }

    // ==============================================
    // CONTAR NOTIFICACIONES PENDIENTES
    // ==============================================

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>>
            contarNoLeidas(Principal principal) {

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

    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificacionResponseDTO>
            marcarComoLeida(
                    @PathVariable Long id,
                    Principal principal) {

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

    private String obtenerCorreo(Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        return principal.getName();
    }
}