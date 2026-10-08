package com.proyectocitas.service;

import com.proyectocitas.dto.AppointmentRequestDTO;
import com.proyectocitas.dto.AppointmentResponseDTO;
import com.proyectocitas.dto.DiagnosisRequestDTO;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;

import com.proyectocitas.mapper.AppointmentMapper;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Usuario;

import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.UsuarioRepository;
import java.time.LocalDate;
import com.proyectocitas.dto.DoctorAgendaDTO;
import java.time.LocalDate;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import org.springframework.http.HttpStatusCode;


@Service
public class AppointmentService {

    private final CitaRepository citaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoCitaRepository estadoCitaRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final AppointmentMapper appointmentMapper;


    public AppointmentService(
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            EstadoCitaRepository estadoCitaRepository,
            HorarioDisponibleRepository horarioRepository,
            AppointmentMapper appointmentMapper) {

        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoCitaRepository = estadoCitaRepository;
        this.horarioRepository = horarioRepository;
        this.appointmentMapper = appointmentMapper;
    }


    // =================================================
    // CREAR CITA
    // =================================================

    @Transactional
    public AppointmentResponseDTO agendarCita(
            AppointmentRequestDTO request,
            String emailPaciente) {


        // 1. Buscar al paciente autenticado
        Usuario paciente = usuarioRepository
                .findByCorreo(emailPaciente)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Paciente autenticado no encontrado"
                        )
                );


        // 2. Buscar el horario solicitado
        HorarioDisponible horario = horarioRepository
                .findById(request.getIdHorario())
                .orElseThrow(() ->
                        new  ResourceNotFoundException(
                                "El horario solicitado no existe"
                        )
                );


        // 3. Comprobar que esté disponible
        if (horario.getEstado()
                != HorarioDisponible.EstadoHorario.DISPONIBLE) {

            throw new ResourceNotFoundException(
                    "El horario seleccionado no está disponible"
            );
        }


        // 4. Comprobar que no exista ya una cita
        // asociada a ese horario
        if (citaRepository
                .existsByHorario_IdHorario(
                        horario.getIdHorario())) {

            throw new ScheduleConflictException(
                    "El horario ya tiene una cita registrada"
            );
        }


        // 5. Obtener el estado inicial PENDIENTE
        EstadoCita estadoPendiente =
                estadoCitaRepository
                        .findByNombre(
                                EstadoCita.NombreEstado.PENDIENTE
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "No existe el estado PENDIENTE"
                                )
                        );


        // 6. Crear la cita
        Cita cita =
                appointmentMapper.toEntity(request);

        cita.setPaciente(paciente);
        cita.setHorario(horario);
        cita.setEstado(estadoPendiente);

        cita.setMotivo(
                request.getMotivo().trim()
        );


        // 7. Cambiar el horario a RESERVADO
        horario.setEstado(
                HorarioDisponible.EstadoHorario.RESERVADO
        );

        horarioRepository.save(horario);


        // 8. Guardar la cita
        Cita citaGuardada =
                citaRepository.save(cita);


        // 9. Devolver DTO
        return appointmentMapper
                .toDTO(citaGuardada);
    }


    // =================================================
    // REGISTRAR DIAGNÓSTICO
    // =================================================

    @Transactional
    public AppointmentResponseDTO registrarDiagnostico(
            Long idCita,
            DiagnosisRequestDTO diagnosisRequest,
            String emailMedico) {


        Usuario usuarioAutenticado =
                usuarioRepository
                        .findByCorreo(emailMedico)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Usuario autenticado no encontrado"
                                )
                        );


        Cita cita = citaRepository
                .findById(idCita)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "La cita médica no existe"
                        )
                );


        if (cita.getHorario() == null
                || cita.getHorario().getMedico() == null
                || cita.getHorario()
                        .getMedico()
                        .getUsuario() == null) {

            throw new ResourceConflictException(
                    "La cita no tiene un médico asignado correctamente"
            );
        }


        Usuario medicoAsignado =
                cita.getHorario()
                        .getMedico()
                        .getUsuario();


        if (!medicoAsignado
                .getIdUsuario()
                .equals(
                        usuarioAutenticado.getIdUsuario()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo el médico asignado puede registrar el diagnóstico"
            );
        }


        if (cita.getEstado() == null
                || cita.getEstado().getNombre()
                != EstadoCita.NombreEstado.PENDIENTE) {

            throw new ResourceConflictException(
                    "Solo se pueden completar citas que estén en estado PENDIENTE"
            );
        }


        EstadoCita estadoCompletada =
                estadoCitaRepository
                        .findByNombre(
                                EstadoCita.NombreEstado.COMPLETADA
                        )
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "No se encontró el estado COMPLETADA"
                                )
                        );


        cita.setDiagnosticoReceta(
                diagnosisRequest
                        .getDiagnosticoReceta()
                        .trim()
        );

        cita.setObservaciones(
                diagnosisRequest.getObservaciones()
        );

        cita.setEstado(estadoCompletada);


        Cita citaActualizada =
                citaRepository.save(cita);


        return appointmentMapper
                .toDTO(citaActualizada);
    }


    // =================================================
    // HISTORIAL DEL PACIENTE
    // =================================================

    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO>
            obtenerMiHistorial(
                    String emailPaciente) {


        Usuario paciente =
                usuarioRepository
                        .findByCorreo(emailPaciente)
                        .orElseThrow(() ->
                                new ResponseStatusException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Usuario autenticado no encontrado"
                                )
                        );


        return citaRepository
                .findByPaciente_IdUsuario(
                        paciente.getIdUsuario()
                )
                .stream()
                .map(appointmentMapper::toDTO)
                .toList();
    }
            @Transactional
public AppointmentResponseDTO cancelarCita(
        Long idCita,
        String correoPaciente) {

    Usuario paciente = usuarioRepository
            .findByCorreo(correoPaciente)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Paciente autenticado no encontrado"
                    )
            );

    Cita cita = citaRepository
            .findById(idCita)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "La cita no existe"
                    )
            );

    if (cita.getPaciente() == null
            || !cita.getPaciente()
                    .getIdUsuario()
                    .equals(paciente.getIdUsuario())) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No puedes cancelar una cita de otro paciente"
        );
    }

    if (cita.getEstado() == null
            || cita.getEstado().getNombre()
            != EstadoCita.NombreEstado.PENDIENTE) {

        throw new ResourceConflictException(
                "Solo se puede cancelar citas pendientes"
        );
    }

    EstadoCita estadoCancelada =
            estadoCitaRepository
                    .findByNombre(
                            EstadoCita.NombreEstado.CANCELADA
                    )
                    .orElseThrow(() ->
                            new ResponseStatusException(
                                    HttpStatus.INTERNAL_SERVER_ERROR,
                                    "No existe el estado CANCELADA"
                            )
                    );

    cita.setEstado(estadoCancelada);

    HorarioDisponible horario =
            cita.getHorario();

    if (horario != null) {

        horario.setEstado(
                HorarioDisponible.EstadoHorario.DISPONIBLE
        );

        horarioRepository.save(horario);
    }

    Cita guardada =
            citaRepository.save(cita);

    return appointmentMapper.toDTO(guardada);
}

// ================================================
// AGEBDA DEL MÉDICO AUTENTICADO
// ================================================
@Transactional(readOnly = true)
public List<DoctorAgendaDTO> obtenerMiAgenda(
        String correoMedico) {

    usuarioRepository
            .findByCorreo(correoMedico)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Usuario autenticado no encontrado"
                    )
            );

    LocalDate hoy = LocalDate.now();
    System.out.println("FECHA DE AGENDA:" +  hoy);

    return citaRepository
            .findAgendaByCorreoMedicoAndFecha(
                    correoMedico,
                    hoy
            )
            .stream()
            .map(cita -> {

                AppointmentResponseDTO citaDTO =
                        appointmentMapper.toDTO(cita);

                return new DoctorAgendaDTO(
                        citaDTO.getIdCita(),
                        citaDTO.getPaciente(),
                        citaDTO.getHoraInicio(),
                        citaDTO.getHoraFin(),
                        citaDTO.getMotivo(),
                        citaDTO.getEstado()
                );
            })
            .toList();
}

// =================================================
// LISTAR TODAS LAS CITAS
// =================================================

@Transactional(readOnly = true)
public List<AppointmentResponseDTO> obtenerTodasLasCitas() {

    return citaRepository
            .findAll()
            .stream()
            .map(appointmentMapper::toDTO)
            .toList();
}
}