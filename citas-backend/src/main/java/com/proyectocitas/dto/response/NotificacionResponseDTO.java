package com.proyectocitas.dto.response;

import java.time.LocalDateTime;

public record NotificacionResponseDTO(
        Long idNotificacion,
        String titulo,
        String mensaje,
        String tipo,
        boolean leida,
        LocalDateTime fechaCreacion
) {
}