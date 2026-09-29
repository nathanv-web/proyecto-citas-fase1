package com.proyectocitas.service;

import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.RolServiceImpl;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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


        Rol resultado =
                service.asignarPermisos(
                        4L,
                        permisos
                );


        assertEquals(
                permisos,
                resultado.getPermisos()
        );
    }
}