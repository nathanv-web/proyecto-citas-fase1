package com.proyectocitas.service;

import com.proyectocitas.model.Rol;
import com.proyectocitas.dto.RolUpdateDTO;
import com.proyectocitas.model.Permiso;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RolService {

    //Obtener todos los roles

    List<Rol> obtenerTodos();

    //Buscar un rol por su ID

    Rol obtenerPorId(Long id);

    //Buscar un rol por su nombre

    Optional<Rol> obtenerPorNombre(String nombre);

    //Guardar un nuevo rol

    Rol guardar(Rol rol);

    //Actualizar solo los datos enviados

    Rol actualizar(RolUpdateDTO rol, Long id);

    //Eliminar un rol por su ID

    void eliminar(Long id);

   //Asignar Permisos
    
    Rol asignarPermisos(
    Long idRol,
            Set<Permiso> permisos
                    
    );


}