package com.proyectocitas.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException, ServletException {


        // =================================================
        // RESPUESTA 403 - PERMISO DENEGADO
        // =================================================

        response.setStatus(
                HttpServletResponse.SC_FORBIDDEN
        );

        response.setContentType(
                "application/json"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );


        String json = """
                {
                    "status": 403,
                    "error": "Forbidden",
                    "message": "Permiso denegado: no tienes permisos para realizar esta acción",
                    "path": "%s"
                }
                """.formatted(
                        request.getRequestURI()
                );


        response.getWriter()
                .write(json);
    }
}