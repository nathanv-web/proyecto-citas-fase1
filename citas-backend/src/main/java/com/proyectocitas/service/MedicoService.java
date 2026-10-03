package com.proyectocitas.service;

import com.proyectocitas.model.Medico;

import java.util.List;
import java.util.Optional;

public interface MedicoService {

    //Obtener todos los medicos

    List<Medico> obtenerTodos();

    //Buscar un medico por su ID

    Optional<Medico> obtenerPorId(Long id);

    //Buscar el perfil de medico por el ID del usuario

    Optional<Medico> obtenerPorUsuario(Long idUsuario);

    //Guardar un nuevo medico

    Medico guardar(Medico medico);

    //Actualizar solo los datos enviados

    Medico actualizar(Medico medico, Long id);

    //Eliminar un medico por su ID

    void eliminar(Long id);
}