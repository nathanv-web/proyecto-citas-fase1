package com.proyectocitas.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioAuthoritiesTest {

    @Test
    void debeDevolverRolesYPermisosComoAuthorities() {

        // Crear rol DOCTOR
        Rol rolDoctor = new Rol(
                "ROLE_DOCTOR",
                "Médico del sistema"
        );

        // Asignar permisos al rol
        rolDoctor.setPermisos(
                Set.of(
                        Permiso.CREAR_HORARIOS,
                        Permiso.VER_CITAS,
                        Permiso.REGISTRAR_DIAGNOSTICO
                )
        );


        // Crear usuario
        Usuario usuario = new Usuario();

        usuario.setRoles(
                Set.of(rolDoctor)
        );


        // Obtener authorities
        Set<String> authorities =
                usuario.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());


        // Debe conservar el rol
        assertTrue(
                authorities.contains("ROLE_DOCTOR")
        );

        // Debe agregar los permisos del rol
        assertTrue(
                authorities.contains("CREAR_HORARIOS")
        );

        assertTrue(
                authorities.contains("VER_CITAS")
        );

        assertTrue(
                authorities.contains("REGISTRAR_DIAGNOSTICO")
        );
    }
}