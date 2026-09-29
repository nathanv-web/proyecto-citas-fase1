package com.proyectocitas.controller;

import com.proyectocitas.dto.AppointmentRequestDTO;
import com.proyectocitas.dto.AppointmentResponseDTO;
import com.proyectocitas.dto.DiagnosisRequestDTO;
import com.proyectocitas.service.AppointmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.List;


@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;


    public AppointmentController(
            AppointmentService appointmentService) {

        this.appointmentService = appointmentService;
    }


    // Crear una nueva cita médica
    @PostMapping
    public ResponseEntity<AppointmentResponseDTO> crearCita(
            @Valid @RequestBody AppointmentRequestDTO request,
            Principal principal) {

        if (principal == null) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        AppointmentResponseDTO cita =
                appointmentService.agendarCita(
                        request,
                        principal.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cita);
    }


    // Registrar diagnóstico y completar cita
    @PutMapping("/{id}/diagnosis")
    public ResponseEntity<AppointmentResponseDTO>
            registrarDiagnostico(
                    @PathVariable("id") Long idCita,
                    @Valid
                    @RequestBody
                    DiagnosisRequestDTO diagnosisRequest,
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


    // Historial del paciente autenticado
    @GetMapping("/my-history")
    public ResponseEntity<List<AppointmentResponseDTO>>
            obtenerMiHistorial(
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
            @PutMapping("/{id}/cancel")
public ResponseEntity<AppointmentResponseDTO> cancelarCita(
        @PathVariable Long id,
        Principal principal) {

    if (principal == null) {
        throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Usuario no autenticado"
        );
    }

    AppointmentResponseDTO respuesta =
            appointmentService.cancelarCita(
                    id,
                    principal.getName()
            );

    return ResponseEntity.ok(respuesta);
}
}
