package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;
import com.proyectocitas.model.Usuario;

import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.UsuarioRepository;

import com.proyectocitas.service.CitaService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CitaServiceImpl implements CitaService {

    private final CitaRepository citaRepository;
    private final HorarioDisponibleRepository horarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoCitaRepository estadoCitaRepository;


    //Constructor para inyectar los repositorios

    public CitaServiceImpl(
            CitaRepository citaRepository,
            HorarioDisponibleRepository horarioRepository,
            UsuarioRepository usuarioRepository,
            EstadoCitaRepository estadoCitaRepository) {

        this.citaRepository = citaRepository;
        this.horarioRepository = horarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.estadoCitaRepository = estadoCitaRepository;
    }


    //Obtener todas las citas

    @Override
    public List<Cita> obtenerTodos() {
        return citaRepository.findAll();
    }


    //Buscar una cita por su ID

    @Override
    public Optional<Cita> obtenerPorId(Long id) {
        return citaRepository.findById(id);
    }


    //Buscar todas las citas de un paciente

    @Override
    public List<Cita> obtenerPorPaciente(Long idUsuario) {

        return citaRepository
                .findByPaciente_IdUsuario(idUsuario);
    }


    //Buscar las citas por su estado

    @Override
    public List<Cita> obtenerPorEstado(
            EstadoCita.NombreEstado nombre) {

        return citaRepository.findByEstado_Nombre(nombre);
    }


    //Buscar una cita por el horario utilizado

    @Override
    public Optional<Cita> obtenerPorHorario(Long idHorario) {

        return citaRepository
                .findByHorario_IdHorario(idHorario);
    }


    //Guardar una nueva cita

    @Override
    @Transactional
    public Cita guardar(Cita cita) {

        //Buscar el horario seleccionado
        HorarioDisponible horarioDB = horarioRepository.findById(
                cita.getHorario().getIdHorario()
        ).get();


        //Verificar que el horario no tenga otra cita
        if (citaRepository.existsByHorario_IdHorario(
                horarioDB.getIdHorario())) {

            throw new RuntimeException(
                    "El horario ya fue reservado"
            );
        }


        //Verificar que el horario esté disponible
        if (horarioDB.getEstado()
                != EstadoHorario.DISPONIBLE) {

            throw new RuntimeException(
                    "El horario no está disponible"
            );
        }


        //Buscar el usuario que funcionará como paciente
        Usuario pacienteDB = usuarioRepository.findById(
                cita.getPaciente().getIdUsuario()
        ).get();


        //Buscar el estado de la cita
        EstadoCita estadoDB = estadoCitaRepository.findById(
                cita.getEstado().getIdEstado()
        ).get();


        //Asignar las entidades existentes a la cita
        cita.setHorario(horarioDB);
        cita.setPaciente(pacienteDB);
        cita.setEstado(estadoDB);


        //Cambiar el horario a reservado
        horarioDB.setEstado(
                EstadoHorario.RESERVADO
        );


        //Guardar el cambio del horario
        horarioRepository.save(horarioDB);


        //Guardar la nueva cita
        return citaRepository.save(cita);
    }


    //Actualizar solamente los campos que tengan información

    @Override
    @Transactional
    public Cita actualizar(
            Cita cita,
            Long id) {

        //Buscar la cita existente
        Cita citaDB =
                citaRepository.findById(id).get();


        //Actualizar paciente si viene con información
        if (cita.getPaciente() != null) {

            Usuario pacienteDB = usuarioRepository.findById(
                    cita.getPaciente().getIdUsuario()
            ).get();

            citaDB.setPaciente(pacienteDB);
        }


        //Actualizar estado si viene con información
        if (cita.getEstado() != null) {

            EstadoCita estadoDB = estadoCitaRepository.findById(
                    cita.getEstado().getIdEstado()
            ).get();

            citaDB.setEstado(estadoDB);
        }


        //Actualizar motivo si viene con valor
        if (cita.getMotivo() != null
                && !cita.getMotivo().isEmpty()) {

            citaDB.setMotivo(
                    cita.getMotivo()
            );
        }


        //Actualizar diagnóstico y receta si viene con valor
        if (cita.getDiagnosticoReceta() != null
                && !cita.getDiagnosticoReceta().isEmpty()) {

            citaDB.setDiagnosticoReceta(
                    cita.getDiagnosticoReceta()
            );
        }


        //Actualizar observaciones si vienen con valor
        if (cita.getObservaciones() != null
                && !cita.getObservaciones().isEmpty()) {

            citaDB.setObservaciones(
                    cita.getObservaciones()
            );
        }


        //Guardar los cambios realizados
        return citaRepository.save(citaDB);
    }


    //Eliminar una cita y liberar su horario

    @Override
    @Transactional
    //Nos ayuda a tratar las operaciones como una sola.
    //Si alguna falla, no se completa la operación.
    public void eliminar(Long id) {

        //Buscar la cita que será eliminada
        Cita citaDB =
                citaRepository.findById(id).get();


        //Obtener el horario relacionado con la cita
        HorarioDisponible horarioDB =
                citaDB.getHorario();


        //Liberar nuevamente el horario
        horarioDB.setEstado(
                EstadoHorario.DISPONIBLE
        );


        //Guardar el cambio del horario
        horarioRepository.save(horarioDB);


        //Eliminar la cita
        citaRepository.deleteById(id);
    }
}