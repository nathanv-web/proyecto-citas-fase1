package com.proyectocitas.security;

import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.UsuarioRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    //Constructor para inyectar el repositorio de usuarios

    public CustomUserDetailsService(
            UsuarioRepository usuarioRepository) {

        this.usuarioRepository = usuarioRepository;
    }


    //Buscar al usuario por correo para que Spring Security lo autentique

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        //Buscar usuario por correo
        Usuario usuario = usuarioRepository
                .findByCorreo(correo)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuario no encontrado: " + correo
                        )
                );

        //Usuario ya implementa UserDetails,
        //por eso podemos devolverlo directamente
        return usuario;
    }
}