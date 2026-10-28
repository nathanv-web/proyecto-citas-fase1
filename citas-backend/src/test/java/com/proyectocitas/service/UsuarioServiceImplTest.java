package com.proyectocitas.service;

import com.proyectocitas.dto.request.UsuarioUpdateRequestDTO;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.UsuarioServiceImpl;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.model.Rol;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

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


        UsuarioUpdateRequestDTO updateDTO =
                new UsuarioUpdateRequestDTO();

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
    @Test
void debeBuscarUsuariosConFiltrosLimpios() {

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

    Usuario usuario = new Usuario();
    usuario.setIdUsuario(1L);
    usuario.setNombre("Ana");

    when(usuarioRepository.buscarConFiltros(
            "ana",
            true,
            "ROLE_ADMIN"
    )).thenReturn(List.of(usuario));

    List<Usuario> resultado =
            service.buscarConFiltros(
                    "  ana  ",
                    true,
                    "  ROLE_ADMIN  "
            );

    assertEquals(1, resultado.size());
    assertEquals(usuario, resultado.get(0));

    verify(usuarioRepository)
            .buscarConFiltros(
                    "ana",
                    true,
                    "ROLE_ADMIN"
            );
}
        @Test
void noDebeDesactivarAlUltimoAdministradorActivo() {

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

    Rol rolAdmin = new Rol();
    rolAdmin.setIdRol(1L);
    rolAdmin.setNombre("ROLE_ADMIN");

    Usuario administrador = new Usuario();
    administrador.setIdUsuario(1L);
    administrador.setActivo(true);
    administrador.getRoles().add(rolAdmin);

    when(usuarioRepository.findById(1L))
            .thenReturn(Optional.of(administrador));

    when(
            usuarioRepository
                    .countByActivoTrueAndRoles_Nombre(
                            "ROLE_ADMIN"
                    )
    ).thenReturn(1L);

    assertThrows(
            ResourceConflictException.class,
            () -> service.desactivar(1L)
    );

    verify(
            usuarioRepository,
            never()
    ).save(administrador);
}
@Test
void noDebePermitirModificarSusPropiosRoles() {

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

    Usuario actor = new Usuario();
    actor.setIdUsuario(1L);
    actor.setCorreo("admin@citanova.com");
    actor.setActivo(true);

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    actor,
                    null,
                    actor.getAuthorities()
            );

    SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

    try {

        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.asignarRol(
                                1L,
                                Set.of(1L)
                        )
                );

        assertEquals(
                403,
                exception.getStatusCode().value()
        );

        verify(
                usuarioRepository,
                never()
        ).save(actor);

    } finally {

        SecurityContextHolder.clearContext();
    }
}
@Test
void noDebeQuitarRolAdminAlUltimoAdministradorActivo() {

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

    Rol rolAdmin = new Rol();
    rolAdmin.setIdRol(1L);
    rolAdmin.setNombre("ROLE_ADMIN");

    Rol rolPaciente = new Rol();
    rolPaciente.setIdRol(2L);
    rolPaciente.setNombre("ROLE_PATIENT");

    Usuario actor = new Usuario();
    actor.setIdUsuario(99L);
    actor.setActivo(true);

    Usuario administradorObjetivo = new Usuario();
    administradorObjetivo.setIdUsuario(1L);
    administradorObjetivo.setActivo(true);
    administradorObjetivo.getRoles().add(rolAdmin);

    UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(
                    actor,
                    null,
                    actor.getAuthorities()
            );

    SecurityContextHolder
            .getContext()
            .setAuthentication(authentication);

    when(usuarioRepository.findById(1L))
            .thenReturn(Optional.of(administradorObjetivo));

    when(rolRepository.findById(2L))
            .thenReturn(Optional.of(rolPaciente));

    when(
            usuarioRepository
                    .countByActivoTrueAndRoles_Nombre(
                            "ROLE_ADMIN"
                    )
    ).thenReturn(1L);

    try {

        assertThrows(
                ResourceConflictException.class,
                () -> service.asignarRol(
                        1L,
                        Set.of(2L)
                )
        );

        verify(
                usuarioRepository,
                never()
        ).save(administradorObjetivo);

    } finally {

        SecurityContextHolder.clearContext();
    }
}
}