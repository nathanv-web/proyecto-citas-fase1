package com.proyectocitas.exception;

public class ScheduleConflictException extends RuntimeException {

    public ScheduleConflictException(String mensaje) {
        super(mensaje);
    }
}