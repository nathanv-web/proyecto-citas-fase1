package com.proyectocitas.service;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaReservadaEvent(
        Long idCita,
        Long idUsuarioMedico,
        String correoPaciente,
        LocalDate fecha,
        LocalTime horaInicio
) {
}