package com.proyectocitas.service;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita.NombreEstado;

import java.util.List;
import java.util.Optional;

public interface CitaService {

    //Obtener todas las citas médicas

    List<Cita> obtenerTodos();

    //Buscar una cita por su ID

    Optional<Cita> obtenerPorId(Long id);

    //Buscar todas las citas de un paciente

    List<Cita> obtenerPorPaciente(Long idUsuario);

    //Buscar citas por su estado

    List<Cita> obtenerPorEstado(NombreEstado nombre);

    //Buscar una cita por el horario utilizado

    Optional<Cita> obtenerPorHorario(Long idHorario);

    //Guardar una nueva cita médica

    Cita guardar(Cita citaMedica);

    //Actualizar solamente los datos enviados

    Cita actualizar(Cita citaMedica, Long id);

    //Eliminar una cita médica por su ID

    void eliminar(Long id);
}