package com.proyectocitas.service;

import com.proyectocitas.model.Rol;

import java.util.List;
import java.util.Optional;

public interface RolService {

    //Obtener todos los roles

    List<Rol> obtenerTodos();

    //Buscar un rol por su ID

    Optional<Rol> obtenerPorId(Long id);

    //Buscar un rol por su nombre

    Optional<Rol> obtenerPorNombre(String nombre);

    //Guardar un nuevo rol

    Rol guardar(Rol rol);

    //Actualizar solo los datos enviados

    Rol actualizar(Rol rol, Long id);

    //Eliminar un rol por su ID

    void eliminar(Long id);
}