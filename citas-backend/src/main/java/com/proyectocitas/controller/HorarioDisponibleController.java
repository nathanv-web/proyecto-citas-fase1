package com.proyectocitas.controller;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.service.HorarioDisponibleService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/schedules")
public class HorarioDisponibleController {

    private final HorarioDisponibleService horarioService;

    public HorarioDisponibleController(
            HorarioDisponibleService horarioService) {

        this.horarioService = horarioService;
    }

    @GetMapping("/available")
    public ResponseEntity<List<HorarioDisponible>> obtenerHorariosDisponibles(
            @RequestParam Long medicoId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        List<HorarioDisponible> horarios =
                horarioService.obtenerHorariosDisponibles(
                        medicoId,
                        fecha
                );

        return ResponseEntity.ok(horarios);
    }
}