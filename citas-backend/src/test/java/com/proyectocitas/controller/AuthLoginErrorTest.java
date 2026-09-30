package com.proyectocitas.controller;

import com.proyectocitas.exception.GlobalExceptionHandler;
import com.proyectocitas.mapper.UsuarioMapper;
import com.proyectocitas.security.JwtService;
import com.proyectocitas.service.UsuarioService;

import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


class AuthLoginErrorTest {

    @Test
    void loginConCredencialesIncorrectasDebeRetornar401()
            throws Exception {

        // =============================================
        // DEPENDENCIAS SIMULADAS
        // =============================================

        AuthenticationManager authenticationManager =
                mock(AuthenticationManager.class);

        JwtService jwtService =
                mock(JwtService.class);

        UsuarioService usuarioService =
                mock(UsuarioService.class);

        UsuarioMapper usuarioMapper =
                mock(UsuarioMapper.class);


        // =============================================
        // SIMULAR CREDENCIALES INCORRECTAS
        // =============================================

        when(authenticationManager.authenticate(any()))
                .thenThrow(
                        new BadCredentialsException(
                                "Credenciales incorrectas"
                        )
                );


        // =============================================
        // CONTROLLER
        // =============================================

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
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();


        // =============================================
        // PETICIÓN
        // =============================================

        mockMvc.perform(
                post("/api/v1/auth/login")

                        .contentType(
                                MediaType.APPLICATION_JSON
                        )

                        .content(
                                """
                                {
                                    "email": "doctor@clinica.com",
                                    "password": "incorrecta"
                                }
                                """
                        )
        )

        // =============================================
        // RESPUESTA ESPERADA
        // =============================================

        .andExpect(
                status().isUnauthorized()
        )

        .andExpect(
                jsonPath("$.status")
                        .value(401)
        )

        .andExpect(
                jsonPath("$.error")
                        .value("Unauthorized")
        )

        .andExpect(
                jsonPath("$.message")
                        .value("Credenciales inválidas")
        )

        .andExpect(
                jsonPath("$.path")
                        .value("/api/v1/auth/login")
        );
    }
}