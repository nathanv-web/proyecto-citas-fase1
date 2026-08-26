package com.proyectocitas.service;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HorarioDisponibleService {

    //Obtener todos los horarios registrados

    List<HorarioDisponible> obtenerTodos();


    //Buscar un horario por su ID

    Optional<HorarioDisponible> obtenerPorId(Long id);


    //Buscar todos los horarios de un medico

    List<HorarioDisponible> obtenerPorMedico(Long idMedico);


    //Buscar horarios por su estado

    List<HorarioDisponible> obtenerPorEstado(
            EstadoHorario estado
    );


    //Buscar horarios de un medico en una fecha específica

    List<HorarioDisponible> obtenerPorMedicoYFecha(
            Long idMedico,
            LocalDate fecha
    );


    //Guardar un nuevo horario

    HorarioDisponible guardar(
            HorarioDisponible horario
    );


    //Actualizar solamente los datos enviados

    HorarioDisponible actualizar(
            HorarioDisponible horario,
            Long id
    );


    //Eliminar un horario por su ID

    void eliminar(Long id);
}