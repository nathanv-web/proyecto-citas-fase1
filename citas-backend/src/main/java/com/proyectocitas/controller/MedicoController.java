package com.proyectocitas.controller;

import com.proyectocitas.model.Medico;
import com.proyectocitas.repository.MedicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/doctors")
public class MedicoController {

    @Autowired
    private MedicoRepository medicoRepository;

    @GetMapping
    public List<Medico> obtenerMedicos(@RequestParam(required = false) String especialidad) {
        List<Medico> medicos = medicoRepository.findAll();
        
        if (especialidad != null && !especialidad.isEmpty()) {
            return medicos.stream()
                    .filter(m -> m.getEspecialidad() != null && 
                            m.getEspecialidad().getNombre().equalsIgnoreCase(especialidad))
                    .collect(Collectors.toList());
        }
        
        return medicos;
    }

    @PostMapping
    public Medico crearMedico(@RequestBody Medico medico) {
        return medicoRepository.save(medico);
    }
}