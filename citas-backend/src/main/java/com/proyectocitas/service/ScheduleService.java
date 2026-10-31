package com.proyectocitas.service;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;
import com.proyectocitas.model.Medico;

import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;

import com.proyectocitas.model.EstadoCita;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class ScheduleService {

    private final HorarioDisponibleRepository horarioRepository;
    private final MedicoRepository medicoRepository;
    private final CitaRepository citaRepository;


    //Constructor para inyectar los repositorios
    public ScheduleService(
            HorarioDisponibleRepository horarioRepository,
            MedicoRepository medicoRepository,
            CitaRepository citaRepository) {

        this.horarioRepository = horarioRepository;
        this.medicoRepository = medicoRepository;
        this.citaRepository = citaRepository;
    }

    /**
     * Crea el horario mientras mantiene bloqueada la fila del médico. Así,
     * otra creación o edición del mismo médico no puede validar el mismo
     * hueco en paralelo.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HorarioDisponible crearHorarioAtomico(
            Long idMedico,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        Medico medico = bloquearMedicoActivo(idMedico);
        validarIntervalo(fecha, horaInicio, horaFin);
        validarFechaFutura(fecha, horaInicio, "crear");

        if (!noHayTraslape(idMedico, fecha, horaInicio, horaFin, null)) {
            throw new ScheduleConflictException(
                    "El horario se cruza con otro horario existente");
        }

        HorarioDisponible horario = new HorarioDisponible();
        horario.setMedico(medico);
        horario.setFecha(fecha);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        return horarioRepository.save(horario);
    }

    /**
     * Bloquea médico y horario hasta terminar la validación y persistencia.
     * La reserva bloquea la misma fila del horario, por lo que una edición
     * no puede pasar entre la comprobación de citas y el guardado.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public HorarioDisponible actualizarHorarioAtomico(
            Long idHorario,
            Long idMedico,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        bloquearMedicoActivo(idMedico);
        validarIntervalo(fecha, horaInicio, horaFin);

        HorarioDisponible horario = horarioRepository
                .buscarHorarioConBloqueo(idHorario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El horario indicado no existe"));

        if (horario.getMedico() == null
                || !idMedico.equals(horario.getMedico().getIdMedico())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No puedes modificar un horario de otro médico");
        }

        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Guatemala"));
        if (horario.getFecha() == null || horario.getHoraInicio() == null
                || !LocalDateTime.of(horario.getFecha(), horario.getHoraInicio()).isAfter(ahora)) {
            throw new ScheduleConflictException(
                    "No se puede modificar un horario cuya hora ya pasó");
        }
        validarFechaFutura(fecha, horaInicio, "cambiar");

        if (citaRepository.existsByHorario_IdHorario(idHorario)) {
            throw new ScheduleConflictException(
                    "No se puede modificar un horario que ya tiene una cita asociada");
        }

        if (!noHayTraslape(idMedico, fecha, horaInicio, horaFin, idHorario)) {
            throw new ScheduleConflictException(
                    "El horario se cruza con otro horario existente");
        }

        horario.setFecha(fecha);
        horario.setHoraInicio(horaInicio);
        horario.setHoraFin(horaFin);
        return horarioRepository.save(horario);
    }


    //Consultar los horarios disponibles de un medico en una fecha
    public List<HorarioDisponible> consultarHorariosDisponibles(
            Long idMedico,
            LocalDate fecha) {

        //Verificar que el medico exista
        validarMedico(idMedico);

        //Verificar que la fecha tenga valor
        if (fecha == null) {
            throw new IllegalArgumentException(
                    "La fecha es obligatoria"
            );
        }

        // Un horario disponible deja de ofrecerse en cuanto comienza,
        // usando la misma zona horaria de la clínica en todos los clientes.
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Guatemala"));
        return horarioRepository
                .findByMedico_IdMedicoAndFechaAndEstado(  //findByMedico_IdMedicoAndFechaAndEstado
                        idMedico,
                        fecha,
                        EstadoHorario.DISPONIBLE
                )
                .stream()
                .filter(horario -> horario.getFecha() != null
                        && horario.getHoraInicio() != null
                        && LocalDateTime.of(horario.getFecha(), horario.getHoraInicio())
                                .isAfter(ahora))
                .sorted(Comparator.comparing(HorarioDisponible::getHoraInicio))
                .toList();
    }


    //Verificar si un horario puede ser reservado
    public boolean estaDisponible(Long idHorario) {

        //Buscar el horario
        HorarioDisponible horario =
                horarioRepository.findById(idHorario)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El horario no existe"
                                )
                        );

        //Verificar que el estado del horario sea DISPONIBLE
        if (horario.getEstado() != EstadoHorario.DISPONIBLE) {
            return false;
        }

        //Verificar que el horario no tenga una cita asociada
        // Solo bloquear si existe una cita vigente
// Verificar que el horario no tenga una cita vigente
if (citaRepository
        .existsByHorario_IdHorarioAndEstado_NombreNot(
                idHorario,
                EstadoCita.NombreEstado.CANCELADA
        )) {
    return false;
}

// Si todas las validaciones se cumplen,
// el horario está disponible.
return true;
}
    //Validar que un nuevo horario no se traslape con otro horario del medico
    public boolean validarNuevoHorario(
            Long idMedico,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        //Verificar que el medico exista
        validarMedico(idMedico);


        //Verificar que los datos obligatorios tengan valor
        if (fecha == null
                || horaInicio == null
                || horaFin == null) {

            throw new IllegalArgumentException(
                    "La fecha y las horas son obligatorias"
            );
        }


        //Verificar que la hora final sea posterior a la inicial
        if (!horaFin.isAfter(horaInicio)) {

            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la hora de inicio"
            );
        }


        //Buscar todos los horarios del medico en esa fecha
        List<HorarioDisponible> horariosExistentes =
                horarioRepository
                        .findByMedico_IdMedicoAndFecha(
                                idMedico,
                                fecha
                        );


        //Comparar el nuevo horario con los existentes
        for (HorarioDisponible horario : horariosExistentes) {

            if (hayTraslape(
                    horaInicio,
                    horaFin,
                    horario.getHoraInicio(),
                    horario.getHoraFin())) {

                return false;
            }
        }

        return true;
    }


    //Determinar si dos horarios se traslapan
    public boolean hayTraslape(
            LocalTime inicioNuevo,
            LocalTime finNuevo,
            LocalTime inicioExistente,
            LocalTime finExistente) {

        return inicioNuevo.isBefore(finExistente)
                && finNuevo.isAfter(inicioExistente);
    }


    //Verificar que el medico exista
    private void validarMedico(Long idMedico) {

        //Verificar que el ID tenga valor
        if (idMedico == null) {

            throw new IllegalArgumentException(
                    "El identificador del medico es obligatorio"
            );
        }


        Medico medico = medicoRepository.findById(idMedico)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El medico indicado no existe"));
        if (medico.getEstado() != Medico.EstadoMedico.ACTIVO) {
            throw new ResourceNotFoundException(
                    "El médico no está disponible");
        }
    }

    private Medico bloquearMedicoActivo(Long idMedico) {
        if (idMedico == null) {
            throw new IllegalArgumentException(
                    "El identificador del medico es obligatorio");
        }
        Medico medico = medicoRepository.buscarMedicoConBloqueo(idMedico)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El medico indicado no existe"));
        if (medico.getEstado() != Medico.EstadoMedico.ACTIVO) {
            throw new ResourceNotFoundException(
                    "El médico no está disponible");
        }
        return medico;
    }

    private void validarIntervalo(
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        if (fecha == null || horaInicio == null || horaFin == null) {
            throw new IllegalArgumentException(
                    "La fecha y las horas son obligatorias");
        }
        if (!horaFin.isAfter(horaInicio)) {
            throw new IllegalArgumentException(
                    "La hora de fin debe ser posterior a la hora de inicio");
        }
    }

    private void validarFechaFutura(
            LocalDate fecha,
            LocalTime horaInicio,
            String accion) {
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Guatemala"));
        if (!LocalDateTime.of(fecha, horaInicio).isAfter(ahora)) {
            String mensaje = "crear".equals(accion)
                    ? "No se puede crear un horario en una fecha u hora que ya pasó"
                    : "No se puede cambiar el horario a una fecha u hora que ya pasó";
            throw new ScheduleConflictException(mensaje);
        }
    }

    private boolean noHayTraslape(
            Long idMedico,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Long idHorarioIgnorado) {
        return horarioRepository.findByMedico_IdMedicoAndFecha(idMedico, fecha)
                .stream()
                .filter(horario -> idHorarioIgnorado == null
                        || !idHorarioIgnorado.equals(horario.getIdHorario()))
                .noneMatch(horario -> hayTraslape(
                        horaInicio, horaFin,
                        horario.getHoraInicio(), horario.getHoraFin()));
    }
    public boolean validarHorarioActualizado(
        Long idHorario,
        Long idMedico,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin) {

    if (idHorario == null) {
        throw new IllegalArgumentException(
                "El identificador del horario es obligatorio"
        );
    }

    if (idMedico == null) {
        throw new IllegalArgumentException(
                "El identificador del medico es obligatorio"
        );
    }

    if (fecha == null || horaInicio == null || horaFin == null) {
        throw new IllegalArgumentException(
                "La fecha y las horas son obligatorias"
        );
    }

    if (!horaFin.isAfter(horaInicio)) {
        throw new IllegalArgumentException(
                "La hora de fin debe ser posterior a la hora de inicio"
        );
    }

    List<HorarioDisponible> horarios =
            horarioRepository
                    .findByMedico_IdMedicoAndFecha(
                            idMedico,
                            fecha
                    );

    return horarios
            .stream()

            // Ignoramos el horario que estamos editando
            .filter(horario ->
                    !horario
                            .getIdHorario()
                            .equals(idHorario)
            )

            // Verificar si se cruza con otro
            .noneMatch(horario ->
                    hayTraslape(
                            horaInicio,
                            horaFin,
                            horario.getHoraInicio(),
                            horario.getHoraFin()
                    )
            );
}
}
