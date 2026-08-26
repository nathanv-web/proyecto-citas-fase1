package com.proyectocitas.service;

import com.proyectocitas.model.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioService {

    List<Usuario> obtenerTodos();

    Optional<Usuario> obtenerPorId(Long id);

    Optional<Usuario> obtenerPorCorreo(String correo);

    Usuario guardar(Usuario usuario);

    Usuario actualizar(Long id, Usuario usuario);

    void eliminar(Long id);
}