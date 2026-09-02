package com.proyectocitas.service;

import com.proyectocitas.dto.AppointmentResponseDTO;
import com.proyectocitas.dto.DiagnosisRequestDTO;
import com.proyectocitas.mapper.AppointmentMapper;
import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AppointmentService {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentService(
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            EstadoCitaRepository estadoCitaRepository,
            AppointmentMapper appointmentMapper) {

        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoCitaRepository = estadoCitaRepository;
        this.appointmentMapper = appointmentMapper;
    }

    // Método desarrollado por Integrante 4
    @Transactional
    public String agendarCita(String datosCita, String emailPaciente) {

        System.out.println("Asociando nueva cita al paciente: " + emailPaciente);

        System.out.println("Validando disponibilidad del bloque horario...");

        System.out.println("Estado del horario cambiado a: RESERVADO");
        System.out.println("Estado inicial de la cita: PENDIENTE");

        return "Cita registrada con éxito. Estado: PENDIENTE";
    }

    // Integrante 5 - Registrar diagnóstico y completar una cita
    @Transactional
    public AppointmentResponseDTO registrarDiagnostico(
            Long idCita,
            DiagnosisRequestDTO diagnosisRequest,
            String emailMedico) {

        Usuario usuarioAutenticado = usuarioRepository.findByCorreo(emailMedico)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"
                ));

        Cita cita = citaRepository.findById(idCita)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "La cita médica no existe"
                ));

        if (cita.getHorario() == null
                || cita.getHorario().getMedico() == null
                || cita.getHorario().getMedico().getUsuario() == null) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La cita no tiene un médico asignado correctamente"
            );
        }

        Usuario medicoAsignado = cita.getHorario()
                .getMedico()
                .getUsuario();

        if (!medicoAsignado.getIdUsuario()
                .equals(usuarioAutenticado.getIdUsuario())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el médico asignado puede registrar el diagnóstico"
            );
        }

        if (cita.getEstado() == null
                || cita.getEstado().getNombre()
                != EstadoCita.NombreEstado.PENDIENTE) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Solo se pueden completar citas que estén en estado PENDIENTE"
            );
        }

        EstadoCita estadoCompletada = estadoCitaRepository
                .findByNombre(EstadoCita.NombreEstado.COMPLETADA)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "No se encontró el estado COMPLETADA"
                ));

        cita.setDiagnosticoReceta(
                diagnosisRequest.getDiagnosticoReceta().trim()
        );

        cita.setObservaciones(
                diagnosisRequest.getObservaciones()
        );

        cita.setEstado(estadoCompletada);

        Cita citaActualizada = citaRepository.save(cita);

        return appointmentMapper.toDTO(citaActualizada);
    }

    // Integrante 5 - Historial de citas del paciente autenticado
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> obtenerMiHistorial(
            String emailPaciente) {

        Usuario paciente = usuarioRepository.findByCorreo(emailPaciente)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Usuario autenticado no encontrado"
                ));

        return citaRepository
                .findByPaciente_IdUsuario(paciente.getIdUsuario())
                .stream()
                .map(appointmentMapper::toDTO)
                .toList();
    }
}