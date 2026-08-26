package com.proyectocitas.controller;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.service.ScheduleService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;


    //Constructor para inyectar el servicio

    public ScheduleController(
            ScheduleService scheduleService) {

        this.scheduleService = scheduleService;
    }


    //Consultar horarios disponibles de un medico por fecha

    @GetMapping("/available")
    public List<HorarioDisponible> consultarDisponibilidad(

            @RequestParam Long idMedico,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {

        return scheduleService
                .consultarHorariosDisponibles(
                        idMedico,
                        fecha
                );
    }
}