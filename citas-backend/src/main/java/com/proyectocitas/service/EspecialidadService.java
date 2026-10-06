package com.proyectocitas.service;

import com.proyectocitas.model.Especialidad;

import java.util.List;
import java.util.Optional;

public interface EspecialidadService {

    // Obtener todas las especialidades
    List<Especialidad> obtenerTodos();

    // Buscar por ID
    Optional<Especialidad> obtenerPorId(Long id);

    // Buscar por nombre
    Optional<Especialidad> obtenerPorNombre(String nombre);

    // Crear
    Especialidad guardar(Especialidad especialidad);

    // Actualizar
    Especialidad actualizar(
            Especialidad especialidad,
            Long id
    );

    // Eliminar
    void eliminar(Long id);
}