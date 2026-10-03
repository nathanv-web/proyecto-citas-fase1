package com.proyectocitas.service;

import com.proyectocitas.model.Usuario;
import com.proyectocitas.dto.UsuarioUpdateDTO;
import java.util.Set;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> obtenerTodos();

    Usuario obtenerPorId(Long id);

    Optional<Usuario> obtenerPorCorreo(String correo);

    Usuario guardar(Usuario usuario);
    
    Usuario registrarPaciente(Usuario usuario);

    Usuario actualizar(Long id, UsuarioUpdateDTO usuario);

    Usuario asignarRol(Long id,
            Set<Long> idsRol );
    
    void desactivar(Long id);
    
}