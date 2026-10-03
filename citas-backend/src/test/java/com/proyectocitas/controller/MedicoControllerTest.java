package com.proyectocitas.controller;

import com.proyectocitas.dto.MedicoRequestDTO;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.mapper.MedicoMapper;
import com.proyectocitas.model.Medico;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MedicoControllerTest {

    @Test
    void debeLanzarDuplicateResourceCuandoUsuarioYaTienePerfilMedico() {

        MedicoRepository medicoRepository =
                mock(MedicoRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        EspecialidadRepository especialidadRepository =
                mock(EspecialidadRepository.class);

        MedicoMapper medicoMapper =
                mock(MedicoMapper.class);


        MedicoController controller =
                new MedicoController(
                        medicoRepository,
                        usuarioRepository,
                        especialidadRepository,
                        medicoMapper
                );


        MedicoRequestDTO request =
                new MedicoRequestDTO();

        request.setIdUsuario(5L);
        request.setIdEspecialidad(2L);


        when(
                medicoRepository
                        .findByUsuario_IdUsuario(5L)
        )
                .thenReturn(
                        Optional.of(new Medico())
                );


        assertThrows(
                DuplicateResourceException.class,
                () -> controller.crearMedico(request)
        );
    }
    @Test
void debeLanzarResourceNotFoundCuandoUsuarioNoExiste() {

    MedicoRepository medicoRepository =
            mock(MedicoRepository.class);

    UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    EspecialidadRepository especialidadRepository =
            mock(EspecialidadRepository.class);

    MedicoMapper medicoMapper =
            mock(MedicoMapper.class);


    MedicoController controller =
            new MedicoController(
                    medicoRepository,
                    usuarioRepository,
                    especialidadRepository,
                    medicoMapper
            );


    MedicoRequestDTO request =
            new MedicoRequestDTO();

    request.setIdUsuario(99L);
    request.setIdEspecialidad(2L);


    // El usuario todavía NO tiene perfil de médico
    when(
            medicoRepository
                    .findByUsuario_IdUsuario(99L)
    )
            .thenReturn(
                    Optional.empty()
            );


    // Pero el usuario 99 NO existe
    when(
            usuarioRepository.findById(99L)
    )
            .thenReturn(
                    Optional.empty()
            );


    assertThrows(
            ResourceNotFoundException.class,
            () -> controller.crearMedico(request)
    );
}

@Test
void debeLanzarResourceNotFoundCuandoEspecialidadNoExiste() {

    MedicoRepository medicoRepository =
            mock(MedicoRepository.class);

    UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    EspecialidadRepository especialidadRepository =
            mock(EspecialidadRepository.class);

    MedicoMapper medicoMapper =
            mock(MedicoMapper.class);


    MedicoController controller =
            new MedicoController(
                    medicoRepository,
                    usuarioRepository,
                    especialidadRepository,
                    medicoMapper
            );


    MedicoRequestDTO request =
            new MedicoRequestDTO();

    request.setIdUsuario(5L);
    request.setIdEspecialidad(99L);


    // El usuario no tiene perfil médico todavía
    when(
            medicoRepository
                    .findByUsuario_IdUsuario(5L)
    )
            .thenReturn(
                    Optional.empty()
            );


    // El usuario sí existe
    Usuario usuario =
            new Usuario();

    usuario.setIdUsuario(5L);


    when(
            usuarioRepository.findById(5L)
    )
            .thenReturn(
                    Optional.of(usuario)
            );


    // Pero la especialidad 99 NO existe
    when(
            especialidadRepository.findById(99L)
    )
            .thenReturn(
                    Optional.empty()
            );


    assertThrows(
            ResourceNotFoundException.class,
            () -> controller.crearMedico(request)
    );
}
    
}