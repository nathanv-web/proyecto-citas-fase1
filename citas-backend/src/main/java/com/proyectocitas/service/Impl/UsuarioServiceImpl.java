package com.proyectocitas.service.Impl;

import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.UsuarioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    //Constructor para inyectar el repositorio de usuarios

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    //Obtener todos los usuarios registrados

    @Override
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    //Buscar un usuario por su ID

    @Override
    public Optional<Usuario> obtenerPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    //Buscar un usuario por su correo

    @Override
    public Optional<Usuario> obtenerPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    //Guardar un nuevo usuario

    @Override
    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    //Actualizar los datos principales de un usuario existente

    @Override
    public Usuario actualizar(Long id, Usuario usuario) {

    //Buscar el usuario existente en la base de datos
    Usuario usuarioDB = usuarioRepository.findById(id).get();

    //Actualizar nombre solo si viene con valor
    if (usuario.getNombre() != null && !usuario.getNombre().isEmpty()) {
        usuarioDB.setNombre(usuario.getNombre());
    }

    //Actualizar apellido solo si viene con valor
    if (usuario.getApellido() != null && !usuario.getApellido().isEmpty()) {
        usuarioDB.setApellido(usuario.getApellido());
    }

    //Actualizar correo solo si viene con valor
    if (usuario.getCorreo() != null && !usuario.getCorreo().isEmpty()) {
        usuarioDB.setCorreo(usuario.getCorreo());
    }

    //Actualizar teléfono solo si viene con valor
    if (usuario.getTelefono() != null && !usuario.getTelefono().isEmpty()) {
        usuarioDB.setTelefono(usuario.getTelefono());
    }

    //Actualizar estado activo solo si viene con valor
    if (usuario.getActivo() != null) {
        usuarioDB.setActivo(usuario.getActivo());
    }

    //Guardar los cambios
    return usuarioRepository.save(usuarioDB);
}

    //Eliminar un usuario por su ID

    @Override
    public void eliminar(Long id) {

        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado");
        }

        usuarioRepository.deleteById(id);
    }
}
