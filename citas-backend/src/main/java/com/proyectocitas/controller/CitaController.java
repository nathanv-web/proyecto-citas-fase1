package com.proyectocitas.controller;

import com.proyectocitas.model.Cita;
import com.proyectocitas.repository.CitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/citas")
public class CitaController {

    @Autowired
    private CitaRepository citaRepository;

    @GetMapping
    public List<Cita> obtenerTodas() {
        return citaRepository.findAll();
    }

    @PostMapping
    public Cita crearCita(@RequestBody Cita cita) {
        return citaRepository.save(cita);
    }
}