package com.proyectocitas.service;

import com.proyectocitas.model.Especialidad;

import java.util.List;
import java.util.Optional;

public interface EspecialidadService {

    //Obtener todas las especialidades

    List<Especialidad> obtenerTodos();

    //Buscar una especialidad por su ID

    Optional<Especialidad> obtenerPorId(Long id);

    //Buscar una especialidad por su nombre

    Optional<Especialidad> obtenerPorNombre(String nombre);

    //Guardar una nueva especialidad

    Especialidad guardar(Especialidad especialidad);

    //Actualizar solo los datos enviados

    Especialidad actualizar(Especialidad especialidad, Long id);

    //Eliminar una especialidad por su ID

    void eliminar(Long id);
}