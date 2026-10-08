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


    // =================================================
    // USUARIO YA TIENE PERFIL MÉDICO
    // =================================================

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
        request.setColegiado("COL-001");
        request.setAniosExperiencia(5);


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


    // =================================================
    // USUARIO NO EXISTE
    // =================================================

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
        request.setColegiado("COL-002");
        request.setAniosExperiencia(3);


        // El usuario todavía no tiene perfil médico
        when(
                medicoRepository
                        .findByUsuario_IdUsuario(99L)
        )
                .thenReturn(
                        Optional.empty()
                );


        // El colegiado tampoco está registrado
        when(
                medicoRepository
                        .findByColegiadoIgnoreCase(
                                "COL-002"
                        )
        )
                .thenReturn(
                        Optional.empty()
                );


        // El usuario no existe
        when(
                usuarioRepository
                        .findById(99L)
        )
                .thenReturn(
                        Optional.empty()
                );


        assertThrows(
                ResourceNotFoundException.class,
                () -> controller.crearMedico(request)
        );
    }


    // =================================================
    // ESPECIALIDAD NO EXISTE
    // =================================================

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
        request.setColegiado("COL-003");
        request.setAniosExperiencia(4);


        // El usuario todavía no tiene perfil médico
        when(
                medicoRepository
                        .findByUsuario_IdUsuario(5L)
        )
                .thenReturn(
                        Optional.empty()
                );


        // El colegiado no está registrado
        when(
                medicoRepository
                        .findByColegiadoIgnoreCase(
                                "COL-003"
                        )
        )
                .thenReturn(
                        Optional.empty()
                );


        // El usuario sí existe
        Usuario usuario =
                new Usuario();

        usuario.setIdUsuario(5L);


        when(
                usuarioRepository
                        .findById(5L)
        )
                .thenReturn(
                        Optional.of(usuario)
                );


        // La especialidad no existe
        when(
                especialidadRepository
                        .findById(99L)
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