package com.proyectocitas.service;

import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.RolServiceImpl;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RolServiceImplTest {

    @Test
    void debeLanzarConflictoCuandoRolEstaAsignadoAUsuarios() {

        RolRepository rolRepository =
                mock(RolRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        RolServiceImpl service =
                new RolServiceImpl(
                        rolRepository,
                        usuarioRepository
                );


        Rol rol = new Rol();

        rol.setIdRol(1L);
        rol.setNombre("ROLE_DOCTOR");


        when(
                rolRepository.findById(1L)
        )
                .thenReturn(
                        Optional.of(rol)
                );


        when(
                usuarioRepository
                        .existsByRoles_IdRol(1L)
        )
                .thenReturn(true);


        assertThrows(
                DuplicateResourceException.class,
                () -> service.eliminar(1L)
        );
    }
}