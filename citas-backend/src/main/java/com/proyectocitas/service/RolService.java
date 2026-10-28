package com.proyectocitas.service;

import com.proyectocitas.model.Rol;
import com.proyectocitas.dto.request.RolUpdateRequestDTO;
import com.proyectocitas.model.Permiso;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RolService {

    //Obtener todos los roles

    List<Rol> obtenerTodos();

    // =================================================
    // BUSCAR ROLES CON PAGINACIÓN
    // =================================================
    // Permite buscar roles parcialmente por nombre.
    // Si search viene vacío, devuelve todos los roles paginados.
    Page<Rol> buscarConPaginacion(
        String search,
        Pageable pageable
);
    //Buscar un rol por su ID

    Rol obtenerPorId(Long id);

    //Buscar un rol por su nombre

    Optional<Rol> obtenerPorNombre(String nombre);

    //Guardar un nuevo rol

    Rol guardar(Rol rol);

    //Actualizar solo los datos enviados

    Rol actualizar(RolUpdateRequestDTO rol, Long id);

    //Eliminar un rol por su ID

    void eliminar(Long id);

   //Asignar Permisos

    Rol asignarPermisos(
    Long idRol,
            Set<Permiso> permisos

    );


}
