package com.proyectocitas.service;

import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.EstadoCita.NombreEstado;

import java.util.List;
import java.util.Optional;

public interface EstadoCitaService {

    //Obtener todos los estados de cita

    List<EstadoCita> obtenerTodos();

    //Buscar un estado por su ID

    Optional<EstadoCita> obtenerPorId(Long id);

    //Buscar un estado por su nombre

    Optional<EstadoCita> obtenerPorNombre(NombreEstado nombre);

    //Guardar un nuevo estado de cita

    EstadoCita guardar(EstadoCita estadoCita);

    //Actualizar solamente los datos enviados

    EstadoCita actualizar(EstadoCita estadoCita, Long id);

    //Eliminar un estado de cita por su ID

    void eliminar(Long id);
}