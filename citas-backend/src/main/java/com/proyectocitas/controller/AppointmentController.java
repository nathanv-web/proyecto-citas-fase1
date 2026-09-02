package com.proyectocitas.controller;

import com.proyectocitas.dto.AppointmentResponseDTO;
import com.proyectocitas.dto.DiagnosisRequestDTO;
import com.proyectocitas.service.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;


    // Método desarrollado por Integrante 4
    @PostMapping
    public ResponseEntity<?> crearCita(
            @RequestBody String datosCita,
            Principal principal) {

        String emailPaciente = (principal != null)
                ? principal.getName()
                : "paciente_anonimo@prueba.com";

        String respuesta = appointmentService
                .agendarCita(datosCita, emailPaciente);

        return ResponseEntity.ok(respuesta);
    }


    // Integrante 5 - Registrar diagnóstico y completar cita
    @PutMapping("/{id}/diagnosis")
    public ResponseEntity<AppointmentResponseDTO> registrarDiagnostico(
            @PathVariable("id") Long idCita,
            @Valid @RequestBody DiagnosisRequestDTO diagnosisRequest,
            Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        AppointmentResponseDTO citaActualizada =
                appointmentService.registrarDiagnostico(
                        idCita,
                        diagnosisRequest,
                        principal.getName()
                );

        return ResponseEntity.ok(citaActualizada);
    }


    // Integrante 5 - Historial del paciente autenticado
    @GetMapping("/my-history")
    public ResponseEntity<List<AppointmentResponseDTO>> obtenerMiHistorial(
            Principal principal) {

        if (principal == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        List<AppointmentResponseDTO> historial =
                appointmentService.obtenerMiHistorial(
                        principal.getName()
                );

        return ResponseEntity.ok(historial);
    }
}