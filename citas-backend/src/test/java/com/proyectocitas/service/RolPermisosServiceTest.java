package com.proyectocitas.service;

import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.RolServiceImpl;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RolPermisosServiceTest {

    @Test
    void debeAsignarPermisosAUnRol() {

        RolRepository rolRepository =
                mock(RolRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);


        RolServiceImpl service =
                new RolServiceImpl(
                        rolRepository,
                        usuarioRepository
                );


        Rol rol = new Rol(
                "ROLE_SECRETARY",
                "Personal administrativo"
        );

        rol.setIdRol(4L);


        Set<Permiso> permisos =
                Set.of(
                        Permiso.VER_MEDICOS,
                        Permiso.VER_CITAS,
                        Permiso.CREAR_CITA,
                        Permiso.VER_HORARIOS_DISPONIBLES
                );

        UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
                "admin@citanova.com",
                null,
                permisos.stream()
                        .map(permiso ->
                                new SimpleGrantedAuthority(
                                        permiso.name()
                                )
                        )
                        .toList()
        );

SecurityContextHolder
        .getContext()
        .setAuthentication(authentication);

        when(
                rolRepository.findById(4L)
        )
                .thenReturn(
                        Optional.of(rol)
                );


        when(
                rolRepository.save(rol)
        )
                .thenReturn(rol);

try {

    Rol resultado =
            service.asignarPermisos(
                    4L,
                    permisos
            );

    assertEquals(
            permisos,
            resultado.getPermisos()
    );

} finally {

    SecurityContextHolder.clearContext();
}
    }

    @Test
void noDebeAsignarPermisoQueElUsuarioNoPosee() {

    RolRepository rolRepository =
            mock(RolRepository.class);

    UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    RolServiceImpl service =
            new RolServiceImpl(
                    rolRepository,
                    usuarioRepository
            );

    Rol rol = new Rol(
            "ROLE_SECRETARY",
            "Personal administrativo"
    );

    rol.setIdRol(4L);

    Set<Permiso> permisosSolicitados =
            Set.of(
                    Permiso.VER_MEDICOS,
                    Permiso.ELIMINAR_ROLES
            );

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    "usuario@citanova.com",
                    null,
                    List.of(
                            new SimpleGrantedAuthority(
                                    Permiso.VER_MEDICOS.name()
                            ),
                            new SimpleGrantedAuthority(
                                    Permiso.ASIGNAR_PERMISOS.name()
                            )
                    )
            );

    SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

    when(
            rolRepository.findById(4L)
    ).thenReturn(
            Optional.of(rol)
    );

    try {

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.asignarPermisos(
                                4L,
                                permisosSolicitados
                        )
                );

        assertEquals(
                403,
                exception.getStatusCode().value()
        );

        assertEquals(
                Set.of(),
                rol.getPermisos()
        );

        verify(
                rolRepository,
                never()
        ).save(rol);

    } finally {

        SecurityContextHolder.clearContext();
    }
}
}