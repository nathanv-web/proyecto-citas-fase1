package com.proyectocitas.service;

import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.RolServiceImpl;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

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
    @Test
void debeBuscarRolesPorNombreConPaginacion() {

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
    rol.setNombre("ROLE_ADMIN");

    Pageable pageable =
            PageRequest.of(0, 10);

    Page<Rol> paginaEsperada =
            new PageImpl<>(
                    List.of(rol),
                    pageable,
                    1
            );

    when(
            rolRepository.findByNombreContainingIgnoreCase(
                    "admin",
                    pageable
            )
    ).thenReturn(paginaEsperada);

    Page<Rol> resultado =
            service.buscarConPaginacion(
                    "  admin  ",
                    pageable
            );

    assertEquals(1, resultado.getTotalElements());
    assertEquals(
            "ROLE_ADMIN",
            resultado.getContent().get(0).getNombre()
    );

    verify(rolRepository)
            .findByNombreContainingIgnoreCase(
                    "admin",
                    pageable
            );
        }
}