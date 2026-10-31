package com.proyectocitas.controller;

import com.proyectocitas.dto.response.HorarioDisponibleResponseDTO;
import com.proyectocitas.dto.request.HorarioDisponibleCreateRequestDTO;
import com.proyectocitas.dto.request.HorarioDisponibleUpdateRequestDTO;

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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Comparator;


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
    public ResponseEntity<HorarioDisponibleResponseDTO>
            crearHorario(
                    @Valid
                    @RequestBody
                    HorarioDisponibleCreateRequestDTO request,
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


        HorarioDisponible guardado = scheduleService.crearHorarioAtomico(
                medico.getIdMedico(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin());


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
    public ResponseEntity<List<HorarioDisponibleResponseDTO>>
            obtenerHorariosDisponibles(
                    @RequestParam Long idMedico,
                    @RequestParam LocalDate fecha) {

        List<HorarioDisponibleResponseDTO> horarios =
                scheduleService
                        .consultarHorariosDisponibles(idMedico, fecha)
                        .stream()
                        .map(horarioMapper::toDTO)
                        .toList();


        return ResponseEntity.ok(
                horarios
        );
    }

    @GetMapping("/my-schedules")
    public ResponseEntity<List<HorarioDisponibleResponseDTO>> obtenerMisHorarios(Principal principal) {
        Medico medico = medicoRepository.findByUsuario_Correo(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario autenticado no tiene perfil de médico"));

        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Guatemala"));
        return ResponseEntity.ok(horarioRepository.findByMedico_IdMedico(medico.getIdMedico())
                .stream()
                .filter(horario -> horario.getFecha() != null && horario.getHoraInicio() != null
                        && LocalDateTime.of(horario.getFecha(), horario.getHoraInicio()).isAfter(ahora))
                .sorted(Comparator.comparing(HorarioDisponible::getFecha)
                        .thenComparing(HorarioDisponible::getHoraInicio))
                .map(horarioMapper::toDTO)
                .toList());
    }


    // =================================================
    // ACTUALIZAR HORARIO
    // =================================================

    @PutMapping("/{id}")
    public ResponseEntity<HorarioDisponibleResponseDTO>
            actualizarHorario(
                    @PathVariable Long id,
                    @Valid
                    @RequestBody
                    HorarioDisponibleUpdateRequestDTO request,
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


        HorarioDisponible actualizado = scheduleService.actualizarHorarioAtomico(
                id,
                medico.getIdMedico(),
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin());


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
