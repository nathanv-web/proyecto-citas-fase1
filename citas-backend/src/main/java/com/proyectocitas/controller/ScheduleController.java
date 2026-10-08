package com.proyectocitas.controller;

import com.proyectocitas.dto.HorarioDisponibleDTO;
import com.proyectocitas.dto.HorarioDisponibleRequestDTO;
import com.proyectocitas.dto.HorarioUpdateRequestDTO;

import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;

import com.proyectocitas.mapper.HorarioDisponibleMapper;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Medico;

import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;

import com.proyectocitas.service.ScheduleService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final MedicoRepository medicoRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final HorarioDisponibleMapper horarioMapper;
    private final CitaRepository citaRepository;


    public ScheduleController(
            ScheduleService scheduleService,
            MedicoRepository medicoRepository,
            HorarioDisponibleRepository horarioRepository,
            HorarioDisponibleMapper horarioMapper,
            CitaRepository citaRepository) {

        this.scheduleService = scheduleService;
        this.medicoRepository = medicoRepository;
        this.horarioRepository = horarioRepository;
        this.horarioMapper = horarioMapper;
        this.citaRepository = citaRepository;
    }


    // =================================================
    // CREAR HORARIO
    // =================================================

    @PostMapping
    public ResponseEntity<HorarioDisponibleDTO>
            crearHorario(
                    @Valid
                    @RequestBody
                    HorarioDisponibleRequestDTO request,
                    Principal principal) {

        // ---------------------------------------------
        // BUSCAR MÉDICO AUTENTICADO
        // ---------------------------------------------

        Medico medico =
                medicoRepository
                        .findByUsuario_Correo(
                                principal.getName()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no tiene perfil de médico"
                                )
                        );


        // ---------------------------------------------
        // VALIDAR FECHA Y HORA
        // ---------------------------------------------

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();


        // No crear horarios en fechas pasadas
        if (request.getFecha().isBefore(hoy)) {

            throw new ScheduleConflictException(
                    "No se puede crear un horario en una fecha pasada"
            );
        }


        // Si es hoy, no permitir horas que ya pasaron
        if (request.getFecha().isEqual(hoy)
                && request.getHoraInicio().isBefore(ahora)) {

            throw new ScheduleConflictException(
                    "No se puede crear un horario en una hora que ya pasó"
            );
        }


        // ---------------------------------------------
        // VALIDAR HORARIO
        // ---------------------------------------------

        boolean valido =
                scheduleService
                        .validarNuevoHorario(
                                medico.getIdMedico(),
                                request.getFecha(),
                                request.getHoraInicio(),
                                request.getHoraFin()
                        );


        if (!valido) {

            throw new ScheduleConflictException(
                    "El horario se cruza con otro horario existente"
            );
        }


        // ---------------------------------------------
        // CREAR ENTIDAD
        // ---------------------------------------------

        HorarioDisponible horario =
                horarioMapper.toEntity(request);

        horario.setMedico(medico);


        // ---------------------------------------------
        // GUARDAR
        // ---------------------------------------------

        HorarioDisponible guardado =
                horarioRepository.save(horario);


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        horarioMapper.toDTO(
                                guardado
                        )
                );
    }


    // =================================================
    // CONSULTAR HORARIOS DISPONIBLES
    // =================================================

    @GetMapping("/available")
    public ResponseEntity<List<HorarioDisponibleDTO>>
            obtenerHorariosDisponibles(
                    @RequestParam Long idMedico,
                    @RequestParam LocalDate fecha) {

        List<HorarioDisponibleDTO> horarios =
                horarioRepository
                        .findByMedico_IdMedicoAndFecha(
                                idMedico,
                                fecha
                        )
                        .stream()
                        .map(horarioMapper::toDTO)
                        .toList();


        return ResponseEntity.ok(
                horarios
        );
    }


    // =================================================
    // ACTUALIZAR HORARIO
    // =================================================

    @PutMapping("/{id}")
    public ResponseEntity<HorarioDisponibleDTO>
            actualizarHorario(
                    @PathVariable Long id,
                    @Valid
                    @RequestBody
                    HorarioUpdateRequestDTO request,
                    Principal principal) {


        // ---------------------------------------------
        // BUSCAR MÉDICO AUTENTICADO
        // ---------------------------------------------

        Medico medico =
                medicoRepository
                        .findByUsuario_Correo(
                                principal.getName()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no tiene perfil de médico"
                                )
                        );


        // ---------------------------------------------
        // BUSCAR HORARIO
        // ---------------------------------------------

        HorarioDisponible horario =
                horarioRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El horario indicado no existe"
                                )
                        );


        // ---------------------------------------------
        // VERIFICAR PROPIETARIO
        // ---------------------------------------------

        if (!horario
                .getMedico()
                .getIdMedico()
                .equals(
                        medico.getIdMedico()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes modificar un horario de otro médico"
            );
        }


        // ---------------------------------------------
        // VALIDAR FECHA DEL HORARIO ORIGINAL
        // ---------------------------------------------

        LocalDate hoy = LocalDate.now();
        LocalTime ahora = LocalTime.now();


        // El horario original ya quedó en el pasado
        if (horario.getFecha().isBefore(hoy)) {

            throw new ScheduleConflictException(
                    "No se puede modificar un horario de una fecha pasada"
            );
        }


        // Si el horario es de hoy pero la hora ya pasó
        if (horario.getFecha().isEqual(hoy)
                && horario.getHoraInicio().isBefore(ahora)) {

            throw new ScheduleConflictException(
                    "No se puede modificar un horario cuya hora ya pasó"
            );
        }


        // ---------------------------------------------
        // VALIDAR NUEVA FECHA
        // ---------------------------------------------

        if (request.getFecha().isBefore(hoy)) {

            throw new ScheduleConflictException(
                    "No se puede cambiar el horario a una fecha pasada"
            );
        }


        if (request.getFecha().isEqual(hoy)
                && request.getHoraInicio().isBefore(ahora)) {

            throw new ScheduleConflictException(
                    "No se puede cambiar el horario a una hora que ya pasó"
            );
        }


        // ---------------------------------------------
        // NO MODIFICAR SI YA TIENE CITA
        // ---------------------------------------------

        if (citaRepository
                .existsByHorario_IdHorario(id)) {

            throw new ScheduleConflictException(
                    "No se puede modificar un horario que ya tiene una cita asociada"
            );
        }


        // ---------------------------------------------
        // VALIDAR TRASLAPE
        // ---------------------------------------------

        boolean valido =
                scheduleService
                        .validarHorarioActualizado(
                                id,
                                medico.getIdMedico(),
                                request.getFecha(),
                                request.getHoraInicio(),
                                request.getHoraFin()
                        );


        if (!valido) {

            throw new ScheduleConflictException(
                    "El horario se cruza con otro horario existente"
            );
        }


        // ---------------------------------------------
        // ACTUALIZAR DATOS
        // ---------------------------------------------

        horario.setFecha(
                request.getFecha()
        );

        horario.setHoraInicio(
                request.getHoraInicio()
        );

        horario.setHoraFin(
                request.getHoraFin()
        );


        HorarioDisponible actualizado =
                horarioRepository.save(
                        horario
                );


        return ResponseEntity.ok(
                horarioMapper.toDTO(
                        actualizado
                )
        );
    }


    // =================================================
    // ELIMINAR HORARIO
    // =================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
            eliminarHorario(
                    @PathVariable Long id,
                    Principal principal) {


        // ---------------------------------------------
        // BUSCAR MÉDICO AUTENTICADO
        // ---------------------------------------------

        Medico medico =
                medicoRepository
                        .findByUsuario_Correo(
                                principal.getName()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario autenticado no tiene perfil de médico"
                                )
                        );


        // ---------------------------------------------
        // BUSCAR HORARIO
        // ---------------------------------------------

        HorarioDisponible horario =
                horarioRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El horario indicado no existe"
                                )
                        );


        // ---------------------------------------------
        // VERIFICAR PROPIETARIO
        // ---------------------------------------------

        if (!horario
                .getMedico()
                .getIdMedico()
                .equals(
                        medico.getIdMedico()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes eliminar un horario de otro médico"
            );
        }


        // ---------------------------------------------
        // NO ELIMINAR SI YA TIENE CITA
        // ---------------------------------------------

        if (citaRepository
                .existsByHorario_IdHorario(id)) {

            throw new ScheduleConflictException(
                    "No se puede eliminar un horario que ya tiene una cita asociada"
            );
        }


        // ---------------------------------------------
        // ELIMINAR
        // ---------------------------------------------

        horarioRepository.delete(
                horario
        );


        return ResponseEntity
                .noContent()
                .build();
    }
}