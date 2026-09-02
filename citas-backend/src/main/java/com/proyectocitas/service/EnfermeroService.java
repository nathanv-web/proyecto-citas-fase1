package com.proyectocitas.service;

import com.proyectocitas.model.Enfermero;

import java.util.List;
import java.util.Optional;

public interface EnfermeroService {

    //Obtener todos los enfermeros

    List<Enfermero> obtenerTodos();

    //Buscar un enfermero por su ID

    Optional<Enfermero> obtenerPorId(Long id);

    //Buscar el perfil de enfermero por el ID del usuario

    Optional<Enfermero> obtenerPorUsuario(Long idUsuario);

    //Guardar un nuevo enfermero

    Enfermero guardar(Enfermero enfermero);

    //Actualizar solo los datos enviados

    Enfermero actualizar(Enfermero enfermero, Long id);

    //Eliminar un enfermero por su ID

    void eliminar(Long id);
}