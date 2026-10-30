package com.proyectocitas.service;

import com.proyectocitas.dto.request.AppointmentCreateRequestDTO;
import com.proyectocitas.dto.response.AppointmentResponseDTO;
import com.proyectocitas.dto.request.DiagnosisRequestDTO;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;

import org.springframework.context.ApplicationEventPublisher;

import com.proyectocitas.mapper.AppointmentMapper;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import com.proyectocitas.dto.request.RescheduleRequestDTO;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.Locale;
import java.util.Objects;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Usuario;

import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.UsuarioRepository;
import java.time.LocalDate;
import com.proyectocitas.dto.response.DoctorAgendaResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
    private final ApplicationEventPublisher publicadorEventos;

    public AppointmentService(
            CitaRepository citaRepository,
            UsuarioRepository usuarioRepository,
            EstadoCitaRepository estadoCitaRepository,
            HorarioDisponibleRepository horarioRepository,
            AppointmentMapper appointmentMapper,
            ApplicationEventPublisher publicadorEventos) {
        
        this.citaRepository = citaRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoCitaRepository = estadoCitaRepository;
        this.horarioRepository = horarioRepository;
        this.appointmentMapper = appointmentMapper;
        this.publicadorEventos = publicadorEventos;
    }


    // =================================================
    // CREAR CITA
    // =================================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
            public AppointmentResponseDTO agendarCita(
            AppointmentCreateRequestDTO request,
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
       // Buscar el horario y bloquearlo mientras se reserva
        HorarioDisponible horario = horarioRepository
        .buscarHorarioConBloqueo(request.getIdHorario())
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "El horario solicitado no existe"
                )
        );


        // 3. Comprobar que esté disponible
        if (horario.getEstado()
                != HorarioDisponible.EstadoHorario.DISPONIBLE) {

            throw new ScheduleConflictException(
                    "El horario seleccionado no está disponible"
            );
        }


        // 4. Comprobar que no exista ya una cita
        // asociada a ese horario
        boolean tieneCitaActiva = citaRepository
        .existsByHorario_IdHorarioAndEstado_NombreNot(
                horario.getIdHorario(),
                EstadoCita.NombreEstado.CANCELADA
        );

if (tieneCitaActiva) {
    throw new ScheduleConflictException(
            "El horario ya tiene una cita vigente"
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
        // Validar que el horario tenga un medico registrado
        if (horario.getMedico() == null
        || horario.getMedico().getUsuario() == null
        || horario.getMedico().getUsuario().getIdUsuario() == null) {

        throw new ResourceConflictException(
            "El horario no tiene un medico asignado correctamente"
        );
        }


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

        // 9. Publicar evento de reserva
        publicadorEventos.publishEvent(
        new CitaReservadaEvent(
                citaGuardada.getIdCita(),
                horario.getMedico()
                        .getUsuario()
                        .getIdUsuario(),
                emailPaciente,
                horario.getFecha(),
                horario.getHoraInicio()
        )
);

        // 10. Devolver DTO
        return appointmentMapper.toDTO(citaGuardada);
    }


    // =================================================
    // REGISTRAR DIAGNÓSTICO
    // =================================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
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


        // Obtener el horario relacionado con la cita
        Long idHorario = citaRepository
        .obtenerIdHorarioDeCita(idCita)
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "La cita médica no existe"
                )
        );

        // Bloquear el horario mientras se registra
        // el diagnóstico
        horarioRepository
        .buscarHorarioConBloqueo(idHorario)
        .orElseThrow(() ->
                new ResourceNotFoundException(
                        "El horario no existe"
                )
        );

// Consultar la cita después de adquirir el bloqueo
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
// =================================================
// CANCELAR CITA CON BLOQUEO TRANSACCIONAL
// =================================================

@Transactional(isolation = Isolation.READ_COMMITTED)
public AppointmentResponseDTO cancelarCita(
        Long idCita,
        String correoPaciente,
        String motivoCancelacion) {


    // 1. Buscar al paciente autenticado
    Usuario paciente = usuarioRepository
            .findByCorreo(correoPaciente)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Paciente autenticado no encontrado"
                    )
            );

    // 2. Obtener el horario asociado a la cita
    Long idHorario = citaRepository
            .obtenerIdHorarioDeCita(idCita)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "La cita no existe"
                    )
            );

    // 3. Bloquear el horario durante la cancelacion
    HorarioDisponible horario = horarioRepository
            .buscarHorarioConBloqueo(idHorario)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "El horario no existe"
                    )
            );

    // 4. Consultar la cita despues de adquirir el bloqueo
    Cita cita = citaRepository
            .findById(idCita)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "La cita no existe"
                    )
            );

    // 5. Verificar que pertenezca al paciente
    if (cita.getPaciente() == null
            || !cita.getPaciente()
                    .getIdUsuario()
                    .equals(paciente.getIdUsuario())) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No puedes cancelar una cita de otro paciente"
        );
    }

    // 6. Validar que la cita este pendiente
    if (cita.getEstado() == null
            || cita.getEstado().getNombre()
            != EstadoCita.NombreEstado.PENDIENTE) {

        throw new ResourceConflictException(
                "Solo se pueden cancelar citas pendientes"
        );
    }

    // 7. Obtener el estado CANCELADA
    EstadoCita estadoCancelada = estadoCitaRepository
            .findByNombre(
                    EstadoCita.NombreEstado.CANCELADA
            )
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "No existe el estado CANCELADA"
                    )
            );

    // 8. Cambiar el estado de la cita, Validar y registrar el motivo de cancelacion
String motivoLimpio = motivoCancelacion == null
        ? null
        : motivoCancelacion.trim();

if (motivoLimpio != null && motivoLimpio.isEmpty()) {
    motivoLimpio = null;
}

if (motivoLimpio != null && motivoLimpio.length() > 500) {
    throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "El motivo de cancelacion no puede superar 500 caracteres"
    );
}

// Cambiar el estado de la cita
cita.setEstado(estadoCancelada);
cita.setMotivoCancelacion(motivoLimpio);

    // 9. Liberar el horario si estaba reservado
    if (horario.getEstado()
            == HorarioDisponible.EstadoHorario.RESERVADO) {
        horario.setEstado(
                HorarioDisponible.EstadoHorario.DISPONIBLE
        );
    }

    // 10. Guardar los cambios
    horarioRepository.save(horario);
    Cita guardada = citaRepository.save(cita);

    // 11. Devolver la cita cancelada
    return appointmentMapper.toDTO(guardada);
}

// =================================================
// CANCELAR CITA SIN MOTIVO (COMPATIBILIDAD)
// =================================================

@Transactional(isolation = Isolation.READ_COMMITTED)
public AppointmentResponseDTO cancelarCita(
        Long idCita,
        String correoPaciente) {

    return cancelarCita(idCita, correoPaciente, null);
}

// =================================================
// REPROGRAMAR CITA MEDICA
// =================================================

@Transactional(isolation = Isolation.READ_COMMITTED)
public AppointmentResponseDTO reprogramarCita(
        Long idCita,
        RescheduleRequestDTO request,
        String correoPaciente) {

    // 1. Obtener al paciente autenticado
    Usuario paciente = usuarioRepository
            .findByCorreo(correoPaciente)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Paciente no encontrado"
                    )
            );

    // 2. Obtener el horario actual de la cita
    Long idHorarioAnterior = citaRepository
            .obtenerIdHorarioDeCita(idCita)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "La cita no existe"
                    )
            );

    Long idHorarioNuevo = request.getIdHorarioNuevo();

    // 3. No permitir seleccionar el mismo horario
    if (idHorarioAnterior.equals(idHorarioNuevo)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Debes seleccionar un horario diferente"
        );
    }

    // 4. Bloquear ambos horarios en orden ascendente
    // para reducir el riesgo de interbloqueos.
    Long primerId = Math.min(
            idHorarioAnterior, idHorarioNuevo
    );

    Long segundoId = Math.max(
            idHorarioAnterior, idHorarioNuevo
    );

    HorarioDisponible primero = horarioRepository
            .buscarHorarioConBloqueo(primerId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Uno de los horarios no existe"
                    )
            );

    HorarioDisponible segundo = horarioRepository
            .buscarHorarioConBloqueo(segundoId)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "Uno de los horarios no existe"
                    )
            );

    HorarioDisponible horarioAnterior =
            idHorarioAnterior.equals(primerId)
                    ? primero : segundo;

    HorarioDisponible horarioNuevo =
            idHorarioNuevo.equals(primerId)
                    ? primero : segundo;

    // 5. Consultar nuevamente la cita
    Cita cita = citaRepository.findById(idCita)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "La cita no existe"
                    )
            );

    // 6. Validar que sea del paciente autenticado
    if (cita.getPaciente() == null
            || !Objects.equals(
                    cita.getPaciente().getIdUsuario(),
                    paciente.getIdUsuario())) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No puedes reprogramar citas de otro paciente"
        );
    }

    // 7. Verificar que la cita no haya cambiado
    // mientras se adquirian los bloqueos
    if (cita.getHorario() == null
            || !Objects.equals(
                    cita.getHorario().getIdHorario(),
                    idHorarioAnterior)) {

        throw new ScheduleConflictException(
                "La cita fue modificada. Intenta nuevamente"
        );
    }

    // 8. Solo reprogramar citas pendientes
    if (cita.getEstado() == null
            || cita.getEstado().getNombre()
            != EstadoCita.NombreEstado.PENDIENTE) {

        throw new ResourceConflictException(
                "Solo puedes reprogramar citas pendientes"
        );
    }

    // 9. Mantener el mismo medico
    if (horarioAnterior.getMedico() == null
            || horarioNuevo.getMedico() == null
            || !Objects.equals(
                    horarioAnterior.getMedico().getIdMedico(),
                    horarioNuevo.getMedico().getIdMedico())) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El nuevo horario debe ser del mismo medico"
        );
    }

    // 10. Comprobar disponibilidad
    if (horarioAnterior.getEstado()
            != HorarioDisponible.EstadoHorario.RESERVADO
            || horarioNuevo.getEstado()
            != HorarioDisponible.EstadoHorario.DISPONIBLE) {

        throw new ScheduleConflictException(
                "El horario no esta disponible"
        );
    }

    // 11. Validar que sea una fecha futura
    if (horarioNuevo.getFecha() == null
            || horarioNuevo.getHoraInicio() == null
            || !LocalDateTime.of(
                    horarioNuevo.getFecha(),
                    horarioNuevo.getHoraInicio()
            ).isAfter(LocalDateTime.now())) {

        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El nuevo horario debe ser futuro"
        );
    }

    // 12. Comprobar que no exista otra cita vigente
    boolean ocupado = citaRepository
            .existsByHorario_IdHorarioAndEstado_NombreNot(
                    idHorarioNuevo,
                    EstadoCita.NombreEstado.CANCELADA
            );

    if (ocupado) {
        throw new ScheduleConflictException(
                "El nuevo horario ya tiene una cita vigente"
        );
    }

    // 13. Liberar el horario anterior
    horarioAnterior.setEstado(
            HorarioDisponible.EstadoHorario.DISPONIBLE
    );

    // 14. Reservar el nuevo horario
    horarioNuevo.setEstado(
            HorarioDisponible.EstadoHorario.RESERVADO
    );

    // 15. Cambiar el horario de la cita
    cita.setHorario(horarioNuevo);

    // 16. Guardar los cambios
    horarioRepository.save(horarioAnterior);
    horarioRepository.save(horarioNuevo);

    Cita actualizada = citaRepository.save(cita);

    return appointmentMapper.toDTO(actualizada);
}

/// =================================================
// AGENDA DEL MEDICO CON FILTROS
// =================================================

@Transactional(readOnly = true)
public List<DoctorAgendaResponseDTO> obtenerMiAgenda(
        String correoMedico,
        LocalDate fecha,
        String periodo,
        LocalDate desde,
        LocalDate hasta) {

    // 1. Validar usuario autenticado
    usuarioRepository.findByCorreo(correoMedico)
            .orElseThrow(() ->
                    new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "Usuario autenticado no encontrado"
                    )
            );

    LocalDate inicio;
    LocalDate fin;

    // 2. Consultar un rango personalizado
    if (desde != null || hasta != null) {

        if (desde == null || hasta == null
                || fecha != null || periodo != null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Utiliza desde y hasta sin combinar otros filtros"
            );
        }

        inicio = desde;
        fin = hasta;

    } else {

        // 3. Obtener la fecha de referencia
        LocalDate referencia = fecha == null
                ? LocalDate.now()
                : fecha;

        String tipo = periodo == null || periodo.isBlank()
                ? "dia"
                : periodo.trim().toLowerCase(Locale.ROOT);

        // 4. Calcular el periodo solicitado
        switch (tipo) {

            case "dia" -> {
                inicio = referencia;
                fin = referencia;
            }

            case "semana" -> {
                inicio = referencia.with(DayOfWeek.MONDAY);
                fin = inicio.plusDays(6);
            }

            case "mes" -> {
                inicio = referencia.withDayOfMonth(1);
                fin = referencia.withDayOfMonth(
                        referencia.lengthOfMonth()
                );
            }

            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El periodo debe ser dia, semana o mes"
            );
        }
    }

    // 5. Verificar el orden de las fechas
    if (inicio.isAfter(fin)) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha inicial no puede superar la final"
        );
    }

    // 6. Consultar las citas del medico autenticado
    List<Cita> citas;

    if (inicio.equals(fin)) {
        citas = citaRepository
                .findAgendaByCorreoMedicoAndFecha(
                        correoMedico,
                        inicio
                );
    } else {
        citas = citaRepository
                .findAgendaByCorreoMedicoAndRango(
                        correoMedico,
                        inicio,
                        fin
                );
    }

    // 7. Convertir las citas a DTO
    return citas.stream()
            .map(cita -> {

                AppointmentResponseDTO citaDTO =
                        appointmentMapper.toDTO(cita);

                DoctorAgendaResponseDTO agenda = new DoctorAgendaResponseDTO(
                        citaDTO.getIdCita(),
                        citaDTO.getPaciente(),
                        citaDTO.getHoraInicio(),
                        citaDTO.getHoraFin(),
                        citaDTO.getMotivo(),
                        citaDTO.getEstado()
                );

                agenda.setFecha(citaDTO.getFecha());

                return agenda;
            })
            .toList();
}

// =================================================
// COMPATIBILIDAD CON LA AGENDA ORIGINAL
// =================================================

@Transactional(readOnly = true)
public List<DoctorAgendaResponseDTO> obtenerMiAgenda(
        String correoMedico) {

    return obtenerMiAgenda(
            correoMedico,
            null,
            null,
            null,
            null
    );
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
