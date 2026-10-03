package com.proyectocitas.service;

import com.proyectocitas.dto.UsuarioUpdateDTO;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.UsuarioServiceImpl;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UsuarioServiceImplTest {

    @Test
    void debeLanzarDuplicateResourceCuandoCorreoYaExisteAlActualizar() {

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);

        RolRepository rolRepository =
                mock(RolRepository.class);

        UsuarioServiceImpl service =
                new UsuarioServiceImpl(
                        usuarioRepository,
                        passwordEncoder,
                        rolRepository
                );


        Usuario usuarioExistente = new Usuario();

        usuarioExistente.setIdUsuario(1L);
        usuarioExistente.setCorreo("actual@gmail.com");


        UsuarioUpdateDTO updateDTO =
                new UsuarioUpdateDTO();

        updateDTO.setCorreo("ocupado@gmail.com");


        when(usuarioRepository.findById(1L))
                .thenReturn(
                        Optional.of(usuarioExistente)
                );

        when(usuarioRepository.existsByCorreo(
                "ocupado@gmail.com"))
                .thenReturn(true);


        assertThrows(
                DuplicateResourceException.class,
                () -> service.actualizar(
                        1L,
                        updateDTO
                )
        );
    }
}