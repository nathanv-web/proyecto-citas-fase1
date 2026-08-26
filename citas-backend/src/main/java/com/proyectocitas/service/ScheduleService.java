package com.proyectocitas.service;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;

import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
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

        //Buscar solamente horarios con estado DISPONIBLE
        return horarioRepository
                .findByMedico_IdMedicoAndFechaAndEstado(  //findByMedico_IdMedicoAndFechaAndEstado
                        idMedico,
                        fecha,
                        EstadoHorario.DISPONIBLE
                );
    }


    //Verificar si un horario puede ser reservado
    public boolean estaDisponible(Long idHorario) {

        //Buscar el horario
        HorarioDisponible horario =
                horarioRepository.findById(idHorario)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El horario no existe"
                                )
                        );

        //Verificar que el estado del horario sea DISPONIBLE
        if (horario.getEstado() != EstadoHorario.DISPONIBLE) {
            return false;
        }

        //Verificar que el horario no tenga una cita asociada
        if (citaRepository.existsByHorario_IdHorario(idHorario)) {
            return false;
        }

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


        //Verificar que exista el medico
        if (!medicoRepository.existsById(idMedico)) {

            throw new IllegalArgumentException(
                    "El medico indicado no existe"
            );
        }
    }
}