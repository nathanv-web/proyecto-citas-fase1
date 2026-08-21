package com.proyectocitas.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RecursoNoEncontradoException.class)
public ResponseEntity<Map<String, Object>> manejarRecursoNoEncontrado(
        RecursoNoEncontradoException ex) {

    Map<String, Object> respuesta = new LinkedHashMap<>();

    respuesta.put("fecha", LocalDateTime.now());
    respuesta.put("estado", HttpStatus.NOT_FOUND.value());
    respuesta.put("error", "Recurso no encontrado");
    respuesta.put("mensaje", ex.getMessage());

    return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(respuesta);
}

    @ExceptionHandler(HorarioNoDisponibleException.class)
public ResponseEntity<Map<String, Object>> manejarHorarioNoDisponible(
        HorarioNoDisponibleException ex) {

    Map<String, Object> respuesta = new LinkedHashMap<>();

    respuesta.put("fecha", LocalDateTime.now());
    respuesta.put("estado", HttpStatus.CONFLICT.value());
    respuesta.put("error", "Horario no disponible");
    respuesta.put("mensaje", ex.getMessage());

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(respuesta);
}

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> manejarExcepcionGeneral(Exception ex) {

        Map<String, Object> respuesta = new LinkedHashMap<>();

        respuesta.put("fecha", LocalDateTime.now());
        respuesta.put("estado", HttpStatus.INTERNAL_SERVER_ERROR.value());
        respuesta.put("error", "Error interno del servidor");
        respuesta.put("mensaje", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(respuesta);
    }
}