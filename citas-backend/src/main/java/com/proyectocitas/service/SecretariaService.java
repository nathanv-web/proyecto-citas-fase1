package com.proyectocitas.service;

import com.proyectocitas.model.Secretaria;

import java.util.List;
import java.util.Optional;

public interface SecretariaService {

    //Obtener todas las secretarias

    List<Secretaria> obtenerTodos();

    //Buscar una secretaria por su ID

    Optional<Secretaria> obtenerPorId(Long id);

    //Buscar el perfil de secretaria por el ID del usuario

    Optional<Secretaria> obtenerPorUsuario(Long idUsuario);

    //Guardar una nueva secretaria

    Secretaria guardar(Secretaria secretaria);

    //Actualizar solo los datos enviados

    Secretaria actualizar(Secretaria secretaria, Long id);

    //Eliminar una secretaria por su ID

    void eliminar(Long id);
}