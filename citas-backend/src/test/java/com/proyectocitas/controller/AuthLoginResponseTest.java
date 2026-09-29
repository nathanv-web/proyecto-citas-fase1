package com.proyectocitas.controller;

import com.proyectocitas.mapper.UsuarioMapper;
import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.security.JwtService;
import com.proyectocitas.service.UsuarioService;

import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class AuthLoginResponseTest {


    @Test
    void loginDebeDevolverTokenUsuarioRolesYPermisos()
            throws Exception {


        // =================================================
        // DEPENDENCIAS SIMULADAS
        // =================================================

        AuthenticationManager authenticationManager =
                mock(AuthenticationManager.class);

        JwtService jwtService =
                mock(JwtService.class);

        UsuarioService usuarioService =
                mock(UsuarioService.class);

        UsuarioMapper usuarioMapper =
                mock(UsuarioMapper.class);

        Authentication authentication =
                mock(Authentication.class);


        // =================================================
        // USUARIO AUTENTICADO
        // =================================================

        Usuario usuario =
                mock(Usuario.class);

        Rol rolDoctor =
                mock(Rol.class);


        when(usuario.getIdUsuario())
                .thenReturn(2L);

        when(usuario.getNombre())
                .thenReturn("Carlos");

        when(usuario.getApellido())
                .thenReturn("Medico");

        when(usuario.getCorreo())
                .thenReturn("doctor@clinica.com");


        // =================================================
        // ROL Y PERMISOS
        // =================================================

        when(rolDoctor.getNombre())
                .thenReturn("ROLE_DOCTOR");

        when(rolDoctor.getPermisos())
                .thenReturn(
                        Set.of(
                                Permiso.CREAR_HORARIOS,
                                Permiso.VER_CITAS,
                                Permiso.REGISTRAR_DIAGNOSTICO
                        )
                );


        when(usuario.getRoles())
                .thenReturn(
                        Set.of(
                                rolDoctor
                        )
                );


        // =================================================
        // AUTENTICACIÓN
        // =================================================

        when(
                authenticationManager.authenticate(
                        any(
                                UsernamePasswordAuthenticationToken.class
                        )
                )
        )
                .thenReturn(authentication);


        when(authentication.getPrincipal())
                .thenReturn(usuario);


        when(
                jwtService.generateToken(
                        "doctor@clinica.com"
                )
        )
                .thenReturn(
                        "jwt-prueba"
                );


        // =================================================
        // CONTROLLER
        // =================================================

        AuthController authController =
                new AuthController(
                        authenticationManager,
                        jwtService,
                        usuarioService,
                        usuarioMapper
                );


        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(authController)
                        .build();


        // =================================================
        // PETICIÓN LOGIN
        // =================================================

        mockMvc.perform(
                        post("/api/v1/auth/login")

                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )

                                .content(
                                        """
                                        {
                                            "email": "doctor@clinica.com",
                                            "password": "123456"
                                        }
                                        """
                                )
                )

                // =================================================
                // RESPUESTA ESPERADA
                // =================================================

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.token")
                                .value("jwt-prueba")
                )

                .andExpect(
                        jsonPath("$.tipo")
                                .value("Bearer")
                )

                .andExpect(
                        jsonPath("$.usuario.idUsuario")
                                .value(2)
                )

                .andExpect(
                        jsonPath("$.usuario.nombre")
                                .value("Carlos")
                )

                .andExpect(
                        jsonPath("$.usuario.apellido")
                                .value("Medico")
                )

                .andExpect(
                        jsonPath("$.usuario.correo")
                                .value(
                                        "doctor@clinica.com"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.usuario.roles[0]"
                        )
                                .value(
                                        "ROLE_DOCTOR"
                                )
                )

                .andExpect(
                        jsonPath(
                                "$.usuario.permisos"
                        )
                                .isArray()
                )

                .andExpect(
                        jsonPath(
                                "$.usuario.permisos"
                        )
                                .isNotEmpty()
                );
    }
}