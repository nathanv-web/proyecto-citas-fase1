package com.proyectocitas.exception;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.validation.FieldError;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import tools.jackson.databind.exc.InvalidFormatException;


@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = 
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // =================================================
    // PARÁMETROS OBLIGATORIOS FALTANTES
    // =================================================

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>>
            manejarParametroFaltante(
                    MissingServletRequestParameterException ex,
                    HttpServletRequest request) {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        respuesta.put("message", "Falta el parámetro obligatorio: " + ex.getParameterName());
        respuesta.put("path", request.getRequestURI());

        return ResponseEntity
                .badRequest()
                .body(respuesta);
    }

    // =================================================
    // RECURSO DUPLICADO
    // =================================================

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<Map<String, Object>>
            manejarDuplicateResourceException(
                    DuplicateResourceException ex,
                    HttpServletRequest request) {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", HttpStatus.CONFLICT.value());
        respuesta.put("error", HttpStatus.CONFLICT.getReasonPhrase());
        respuesta.put("message", ex.getMessage());
        respuesta.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(respuesta);
    }


    // =================================================
    // RESPONSE STATUS EXCEPTION
    // =================================================

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>>
            manejarResponseStatusException(
                    ResponseStatusException ex,
                    HttpServletRequest request) {

        int codigo = ex.getStatusCode().value();

        HttpStatus status =
                HttpStatus.resolve(codigo);

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", codigo);
        respuesta.put(
                "error",
                status != null
                        ? status.getReasonPhrase()
                        : "Error"
        );
        respuesta.put(
                "message",
                ex.getReason() != null
                        ? ex.getReason()
                        : "Error en la solicitud"
        );
        respuesta.put("path", request.getRequestURI());

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(respuesta);
    }


    // =================================================
    // ERRORES DE VALIDACIÓN @Valid
    // =================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>>
            manejarValidaciones(
                    MethodArgumentNotValidException ex,
                    HttpServletRequest request) {

        Map<String, String> errores =
                new LinkedHashMap<>();

        for (FieldError error
                : ex.getBindingResult().getFieldErrors()) {

            errores.put(
                    error.getField(),
                    error.getDefaultMessage()
            );
        }

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", HttpStatus.BAD_REQUEST.value());
        respuesta.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
        respuesta.put("message", "Error de validación");
        respuesta.put("errors", errores);
        respuesta.put("path", request.getRequestURI());

        return ResponseEntity
                .badRequest()
                .body(respuesta);
    }
            
         // =================================================
         // CONFLICTO DE HORARIO
         // =================================================

@ExceptionHandler(ScheduleConflictException.class)
public ResponseEntity<Map<String, Object>>
        manejarConflictoHorario(
                ScheduleConflictException ex,
                HttpServletRequest request) {

    Map<String, Object> respuesta =
            new LinkedHashMap<>();

    respuesta.put("timestamp", LocalDateTime.now());
    respuesta.put("status", HttpStatus.CONFLICT.value());
    respuesta.put("error", HttpStatus.CONFLICT.getReasonPhrase());
    respuesta.put("message", ex.getMessage());
    respuesta.put("path", request.getRequestURI());

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(respuesta);
}


    // =================================================
    // RECURSO NO ENCONTRADO
    // =================================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>>
            manejarResourceNotFoundException(
                    ResourceNotFoundException ex,
                    HttpServletRequest request) {

        Map<String, Object> respuesta =
                new LinkedHashMap<>();

        respuesta.put("timestamp", LocalDateTime.now());
        respuesta.put("status", HttpStatus.NOT_FOUND.value());
        respuesta.put("error", HttpStatus.NOT_FOUND.getReasonPhrase());
        respuesta.put("message", ex.getMessage());
        respuesta.put("path", request.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(respuesta);
    }
            
            // =================================================
           // ARGUMENTOS INVÁLIDOS
           // =================================================

@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<Map<String, Object>>
        manejarArgumentoInvalido(
                IllegalArgumentException ex,
                HttpServletRequest request) {

    Map<String, Object> respuesta =
            new LinkedHashMap<>();

    respuesta.put("timestamp", LocalDateTime.now());
    respuesta.put("status", HttpStatus.BAD_REQUEST.value());
    respuesta.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
    respuesta.put("message", ex.getMessage());
    respuesta.put("path", request.getRequestURI());

    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(respuesta);
}
        
        // =================================================
// CONFLICTO DE RECURSO
// =================================================

@ExceptionHandler(ResourceConflictException.class)
public ResponseEntity<Map<String, Object>>
        manejarConflictoRecurso(
                ResourceConflictException ex,
                HttpServletRequest request) {

    Map<String, Object> respuesta =
            new LinkedHashMap<>();

    respuesta.put("timestamp", LocalDateTime.now());
    respuesta.put("status", HttpStatus.CONFLICT.value());
    respuesta.put("error", HttpStatus.CONFLICT.getReasonPhrase());
    respuesta.put("message", ex.getMessage());
    respuesta.put("path", request.getRequestURI());

    return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(respuesta);
}
        
        
// =================================================
// JSON MAL FORMADO / TIPO DE DATO INCORRECTO
// =================================================

@ExceptionHandler(HttpMessageNotReadableException.class)
public ResponseEntity<Map<String, Object>>
        manejarJsonInvalido(
                HttpMessageNotReadableException ex,
                HttpServletRequest request) {

    Map<String, Object> respuesta =
            new LinkedHashMap<>();

    String mensaje =
            "El formato de la solicitud JSON es inválido";


    // Verificar si el problema fue un tipo de dato incorrecto
    if (ex.getCause() instanceof InvalidFormatException invalidFormatException) {

        String campo = "desconocido";

        if (!invalidFormatException.getPath().isEmpty()) {

            campo = invalidFormatException
                    .getPath()
                    .getFirst()
                    .getPropertyName();
        }

        mensaje =
                "El campo '" + campo
                + "' tiene un tipo de dato inválido";
    }


    respuesta.put("timestamp", LocalDateTime.now());
    respuesta.put("status", HttpStatus.BAD_REQUEST.value());
    respuesta.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
    respuesta.put("message", mensaje);
    respuesta.put("path", request.getRequestURI());


    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(respuesta);
}

    // =================================================
    // ERRORES NO CONTROLADOS
    // =================================================

@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>>
        manejarErrorGeneral(
                Exception ex,
                HttpServletRequest request) {

    logger.error(
            "Error no controlado en {}",
            request.getRequestURI(),
            ex
    );

    Map<String, Object> respuesta =
            new LinkedHashMap<>();

    respuesta.put("timestamp", LocalDateTime.now());
    respuesta.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
    respuesta.put("error", HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    respuesta.put("message", "Ocurrió un error interno en el servidor");
    respuesta.put("path", request.getRequestURI());

    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(respuesta);
}
        
        
        
        
}