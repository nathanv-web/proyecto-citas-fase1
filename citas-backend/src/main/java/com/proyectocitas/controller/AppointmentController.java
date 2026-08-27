package com.proyectocitas.controller;

import com.proyectocitas.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    
    @PostMapping
    public ResponseEntity<?> crearCita(@RequestBody String datosCita, Principal principal) {
        
        
        String emailPaciente = (principal != null) ? principal.getName() : "paciente_anonimo@prueba.com";
        
       
        String respuesta = appointmentService.agendarCita(datosCita, emailPaciente);
        
        
        return ResponseEntity.ok(respuesta);
    }
}