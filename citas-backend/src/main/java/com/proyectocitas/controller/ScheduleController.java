package com.proyectocitas.controller;

import com.proyectocitas.dto.HorarioDisponibleDTO;
import com.proyectocitas.dto.HorarioDisponibleRequestDTO;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.mapper.HorarioDisponibleMapper;
import com.proyectocitas.exception.ScheduleConflictException;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Medico;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.service.ScheduleService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MedicoRepository medicoRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final HorarioDisponibleMapper horarioMapper;

    public ScheduleController(
            ScheduleService scheduleService,
            MedicoRepository medicoRepository,
            HorarioDisponibleRepository horarioRepository,
            HorarioDisponibleMapper horarioMapper) {

        this.scheduleService = scheduleService;
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
        this.horarioMapper = horarioMapper;
    }

@GetMapping("/available")
public List<HorarioDisponibleDTO> consultarDisponibilidad(

        @RequestParam Long idMedico,

        @RequestParam
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate fecha) {

    return scheduleService
            .consultarHorariosDisponibles(idMedico, fecha)
            .stream()
            .map(horarioMapper::toDTO)
            .toList();
}

    @PostMapping
    public ResponseEntity<HorarioDisponibleDTO>
            crearHorario(

                    @Valid
                    @RequestBody
                    HorarioDisponibleRequestDTO request) {

        boolean valido =
                scheduleService.validarNuevoHorario(
                        request.getIdMedico(),
                        request.getFecha(),
                        request.getHoraInicio(),
                        request.getHoraFin()
                );

        if (!valido) {

            throw new ScheduleConflictException(
                    "El horario se cruza con otro horario existente"
            );
        }

        Medico medico =
                medicoRepository
                        .findById(request.getIdMedico())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El medico indicado no existe"
                                )
                        );

        HorarioDisponible horario =
                horarioMapper.toEntity(request);

        horario.setMedico(medico);

        horario.setEstado(
                HorarioDisponible
                        .EstadoHorario
                        .DISPONIBLE
        );

        HorarioDisponible guardado =
                horarioRepository.save(horario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        horarioMapper.toDTO(guardado)
                );
    }
}