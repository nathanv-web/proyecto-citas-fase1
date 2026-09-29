package com.proyectocitas.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAuthenticationEntryPoint
        implements AuthenticationEntryPoint {


    // =================================================
    // RESPUESTA 401 - NO AUTENTICADO
    // =================================================
    // Se ejecuta cuando una petición intenta acceder
    // a un recurso protegido sin autenticación válida.

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException, ServletException {


        response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        String json = """
                {
                    "status": 401,
                    "error": "Unauthorized",
                    "message": "Debes iniciar sesión para acceder a este recurso",
                    "path": "%s"
                }
                """.formatted(
                        request.getRequestURI()
                );


        response.getWriter()
                .write(json);
    }
}