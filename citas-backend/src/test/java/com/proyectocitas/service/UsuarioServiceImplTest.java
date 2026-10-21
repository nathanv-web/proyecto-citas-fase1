package com.proyectocitas.service;

import com.proyectocitas.dto.UsuarioUpdateDTO;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.UsuarioServiceImpl;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.model.Rol;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.List;

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
}