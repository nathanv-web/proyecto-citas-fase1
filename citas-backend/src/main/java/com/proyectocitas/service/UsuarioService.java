package com.proyectocitas.service;

import com.proyectocitas.model.Usuario;
import com.proyectocitas.dto.UsuarioUpdateDTO;
import java.util.Set;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> obtenerTodos();

// =================================================
// BUSCAR Y FILTRAR USUARIOS
// =================================================
// Permite buscar usuarios por nombre, apellido o correo
// y aplicar opcionalmente filtros por estado y rol.
List<Usuario> buscarConFiltros(
        String search,
        Boolean activo,
        String rol
);

    Usuario obtenerPorId(Long id);

    Optional<Usuario> obtenerPorCorreo(String correo);

    Usuario guardar(Usuario usuario);
    
    Usuario registrarPaciente(Usuario usuario);

    Usuario actualizar(Long id, UsuarioUpdateDTO usuario);

    Usuario asignarRol(Long id,
            Set<Long> idsRol );
    
    void desactivar(Long id);
    
}