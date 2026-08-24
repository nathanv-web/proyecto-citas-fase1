package com.proyectocitas.controller;

import com.proyectocitas.exception.HorarioNoDisponibleException;
import com.proyectocitas.exception.RecursoNoEncontradoException;
import com.proyectocitas.model.CitaMedica;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.repository.CitaMedicaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
public class CitaMedicaController {

    private final CitaMedicaRepository citaMedicaRepository;
    private final HorarioDisponibleRepository horarioDisponibleRepository;

    public CitaMedicaController(
            CitaMedicaRepository citaMedicaRepository,
            HorarioDisponibleRepository horarioDisponibleRepository) {

        this.citaMedicaRepository = citaMedicaRepository;
        this.horarioDisponibleRepository = horarioDisponibleRepository;
    }

    @GetMapping
    public List<CitaMedica> listarCitas() {
        return citaMedicaRepository.findAll();
    }

    @PostMapping
    @Transactional
    public ResponseEntity<CitaMedica> crearCita(
            @RequestBody CitaMedica citaMedica) {

        HorarioDisponible horario = horarioDisponibleRepository
                .findById(citaMedica.getIdHorario())
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "No existe el horario con id "
                                + citaMedica.getIdHorario()
                        )
                );

        if (!horario.isDisponible()
                || citaMedicaRepository.existsByIdHorario(
                        citaMedica.getIdHorario())) {

            throw new HorarioNoDisponibleException(
                    "El horario seleccionado ya no está disponible"
            );
        }

        CitaMedica citaGuardada =
                citaMedicaRepository.save(citaMedica);

        horario.setDisponible(false);
        horarioDisponibleRepository.save(horario);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(citaGuardada);
    }
}
