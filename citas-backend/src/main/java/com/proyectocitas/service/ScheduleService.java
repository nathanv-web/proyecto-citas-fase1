package com.proyectocitas.service;

import com.proyectocitas.model.Cita;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.MedicoRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScheduleService {

    private static final int DURACION_SLOT_MINUTOS = 30;

    private final CitaRepository citaRepository;
    private final MedicoRepository medicoRepository;

    public ScheduleService(
            CitaRepository citaRepository,
            MedicoRepository medicoRepository
    ) {
        this.citaRepository = citaRepository;
        this.medicoRepository = medicoRepository;
    }

    /**
     * Consulta los bloques horarios disponibles para un médico
     * en una fecha determinada.
     */
    public List<LocalDateTime> consultarSlotsDisponibles(
            Long medicoId,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin
    ) {

        validarMedico(medicoId);

        List<LocalDateTime> slotsDisponibles = new ArrayList<>();

        LocalDateTime inicioSlot = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime finJornada = LocalDateTime.of(fecha, horaFin);

        while (!inicioSlot.plusMinutes(DURACION_SLOT_MINUTOS)
                .isAfter(finJornada)) {

            LocalDateTime finSlot =
                    inicioSlot.plusMinutes(DURACION_SLOT_MINUTOS);

            if (estaDisponible(medicoId, inicioSlot, finSlot)) {
                slotsDisponibles.add(inicioSlot);
            }

            inicioSlot = finSlot;
        }

        return slotsDisponibles;
    }

    /**
     * Verifica si un bloque horario está disponible.
     */
    public boolean estaDisponible(
            Long medicoId,
            LocalDateTime inicio,
            LocalDateTime fin
    ) {

        validarMedico(medicoId);

        if (inicio == null || fin == null) {
            throw new IllegalArgumentException(
                    "La fecha y hora son obligatorias."
            );
        }

        if (!fin.isAfter(inicio)) {
            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la hora de inicio."
            );
        }

        List<Cita> citasExistentes =
                citaRepository.findByMedicoIdAndFechaHoraBetween(
                        medicoId,
                        inicio.minusMinutes(DURACION_SLOT_MINUTOS),
                        fin
                );

        for (Cita cita : citasExistentes) {

            LocalDateTime inicioExistente = cita.getFechaHora();

            LocalDateTime finExistente =
                    inicioExistente.plusMinutes(DURACION_SLOT_MINUTOS);

            if (hayTraslape(
                    inicio,
                    fin,
                    inicioExistente,
                    finExistente
            )) {
                return false;
            }
        }

        return true;
    }

    /**
     * Determina si dos bloques horarios se traslapan.
     */
    public boolean hayTraslape(
            LocalDateTime inicioNuevo,
            LocalDateTime finNuevo,
            LocalDateTime inicioExistente,
            LocalDateTime finExistente
    ) {

        return inicioNuevo.isBefore(finExistente)
                && finNuevo.isAfter(inicioExistente);
    }

    private void validarMedico(Long medicoId) {

        if (medicoId == null) {
            throw new IllegalArgumentException(
                    "El identificador del médico es obligatorio."
            );
        }

        if (!medicoRepository.existsById(medicoId)) {
            throw new IllegalArgumentException(
                    "El médico indicado no existe."
            );
        }
    }
}