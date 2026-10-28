package com.proyectocitas.service;

import com.proyectocitas.model.Usuario;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class AuditoriaService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(AuditoriaService.class);

    private AuditoriaService() {
        // Evita crear instancias de esta clase de utilidad.
    }

    // =================================================
    // REGISTRAR EVENTO DE AUDITORÍA
    // =================================================
    // Registra únicamente información administrativa:
    // actor, fecha, acción, recurso, resultado y detalle.
    //
    // No deben enviarse contraseñas, tokens ni secretos.
    public static void registrar(
            String accion,
            String recurso,
            String resultado,
            String detalle) {

        String actor =
                obtenerActorAutenticado();

        LOGGER.info(
                "AUDITORIA actor={} fecha={} accion={} recurso={} resultado={} detalle={}",
                limpiar(actor),
                Instant.now(),
                limpiar(accion),
                limpiar(recurso),
                limpiar(resultado),
                limpiar(detalle)
        );
    }

    // =================================================
    // OBTENER ACTOR AUTENTICADO
    // =================================================
    private static String obtenerActorAutenticado() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            return "SISTEMA";
        }

        if (authentication.getPrincipal()
                instanceof Usuario usuario) {

            return usuario.getCorreo() != null
                    ? usuario.getCorreo()
                    : "USUARIO_ID_" + usuario.getIdUsuario();
        }

        return authentication.getName();
    }

    // =================================================
    // LIMPIAR TEXTO PARA EL LOG
    // =================================================
    // Evita saltos de línea que puedan alterar el formato
    // de los registros de auditoría.
    private static String limpiar(String valor) {

        if (valor == null) {
            return "";
        }

        return valor
                .replace("\r", " ")
                .replace("\n", " ");
    }
}