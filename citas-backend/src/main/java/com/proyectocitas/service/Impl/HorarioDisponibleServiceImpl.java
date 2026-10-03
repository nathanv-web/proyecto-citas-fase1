package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Medico;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;

import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;

import com.proyectocitas.service.HorarioDisponibleService;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class HorarioDisponibleServiceImpl
        implements HorarioDisponibleService {

    private final HorarioDisponibleRepository horarioRepository;
    private final MedicoRepository medicoRepository;


    //Constructor para inyectar los repositorios

    public HorarioDisponibleServiceImpl(
            HorarioDisponibleRepository horarioRepository,
            MedicoRepository medicoRepository) {

        this.horarioRepository = horarioRepository;
        this.medicoRepository = medicoRepository;
    }


    //Obtener todos los horarios registrados

    @Override
    public List<HorarioDisponible> obtenerTodos() {
        return horarioRepository.findAll();
    }


    //Buscar un horario por su ID

    @Override
    public Optional<HorarioDisponible> obtenerPorId(Long id) {
        return horarioRepository.findById(id);
    }


    //Buscar todos los horarios de un medico

    @Override
    public List<HorarioDisponible> obtenerPorMedico(Long idMedico) {

        return horarioRepository
                .findByMedico_IdMedico(idMedico);
    }


    //Buscar horarios por su estado

    @Override
    public List<HorarioDisponible> obtenerPorEstado(
            EstadoHorario estado) {

        return horarioRepository.findByEstado(estado);
    }


    //Buscar los horarios de un medico en una fecha específica

    @Override
    public List<HorarioDisponible> obtenerPorMedicoYFecha(
            Long idMedico,
            LocalDate fecha) {

        return horarioRepository
                .findByMedico_IdMedicoAndFecha(
                        idMedico,
                        fecha
                );
    }


    //Guardar un nuevo horario

    @Override
    public HorarioDisponible guardar(
            HorarioDisponible horario) {

        //Buscar el medico relacionado
        if (horario.getMedico() != null) {

            Medico medicoDB = medicoRepository.findById(
                    horario.getMedico().getIdMedico()
            ).get();

            horario.setMedico(medicoDB);
        }

        //Guardar el horario
        return horarioRepository.save(horario);
    }


    //Actualizar solamente los campos que tengan información

    @Override
    public HorarioDisponible actualizar(
            HorarioDisponible horario,
            Long id) {

        //Buscar el horario existente
        HorarioDisponible horarioDB =
                horarioRepository.findById(id).get();


        //Actualizar medico si viene con información
        if (horario.getMedico() != null) {

            Medico medicoDB = medicoRepository.findById(
                    horario.getMedico().getIdMedico()
            ).get();

            horarioDB.setMedico(medicoDB);
        }


        //Actualizar fecha si viene con valor
        if (horario.getFecha() != null) {

            horarioDB.setFecha(
                    horario.getFecha()
            );
        }


        //Actualizar hora de inicio si viene con valor
        if (horario.getHoraInicio() != null) {

            horarioDB.setHoraInicio(
                    horario.getHoraInicio()
            );
        }


        //Actualizar hora de fin si viene con valor
        if (horario.getHoraFin() != null) {

            horarioDB.setHoraFin(
                    horario.getHoraFin()
            );
        }


        //Actualizar estado si viene con valor
        if (horario.getEstado() != null) {

            horarioDB.setEstado(
                    horario.getEstado()
            );
        }


        //Guardar los cambios realizados
        return horarioRepository.save(horarioDB);
    }


    //Eliminar un horario por su ID

    @Override
    public void eliminar(Long id) {

        horarioRepository.deleteById(id);
    }
}
