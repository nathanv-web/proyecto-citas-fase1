package com.proyectocitas.controller;

import com.proyectocitas.dto.response.NotificacionResponseDTO;
import com.proyectocitas.service.NotificacionService;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NotificacionControllerTest {

    private MockMvc mockMvc;
    private NotificacionService service;

    private static final String CORREO =
            "paciente@correo.com";

    @BeforeEach
    void setUp() {

        service = mock(NotificacionService.class);

        NotificacionController controller =
                new NotificacionController(service);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    // =========================================
    // PRUEBA 1: LISTAR NOTIFICACIONES
    // =========================================

    @Test
    void debeListarMisNotificaciones() throws Exception {

        NotificacionResponseDTO aviso =
                new NotificacionResponseDTO(
                        10L,
                        "Nueva cita",
                        "Tienes una nueva cita reservada",
                        "CITA_RESERVADA",
                        false,
                        LocalDateTime.of(2026, 10, 29, 10, 30)
                );

        when(service.listarMias(CORREO))
                .thenReturn(List.of(aviso));

        mockMvc.perform(
                get("/api/v1/notifications")
                        .principal(() -> CORREO)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].idNotificacion").value(10))
        .andExpect(jsonPath("$[0].titulo").value("Nueva cita"))
        .andExpect(jsonPath("$[0].tipo").value("CITA_RESERVADA"))
        .andExpect(jsonPath("$[0].leida").value(false));

        verify(service).listarMias(CORREO);
    }

    // =========================================
    // PRUEBA 2: LISTA VACIA
    // =========================================

    @Test
    void debeRetornarListaVacia() throws Exception {

        when(service.listarMias(CORREO))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/api/v1/notifications")
                        .principal(() -> CORREO)
        )
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));

        verify(service).listarMias(CORREO);
    }

    // =========================================
    // PRUEBA 3: CONTAR NO LEIDAS
    // =========================================

    @Test
    void debeMostrarCantidadNoLeidas() throws Exception {

        when(service.contarNoLeidas(CORREO))
                .thenReturn(3L);

        mockMvc.perform(
                get("/api/v1/notifications/unread-count")
                        .principal(() -> CORREO)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.noLeidas").value(3));

        verify(service).contarNoLeidas(CORREO);
    }

    // =========================================
    // PRUEBA 4: MARCAR COMO LEIDA
    // =========================================

    @Test
    void debeMarcarNotificacionComoLeida() throws Exception {

        NotificacionResponseDTO aviso =
                new NotificacionResponseDTO(
                        10L,
                        "Nueva cita",
                        "Tienes una cita",
                        "CITA_RESERVADA",
                        true,
                        LocalDateTime.of(2026, 10, 29, 10, 30)
                );

        when(service.marcarComoLeida(10L, CORREO))
                .thenReturn(aviso);

        mockMvc.perform(
                patch("/api/v1/notifications/10/read")
                        .principal(() -> CORREO)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.idNotificacion").value(10))
        .andExpect(jsonPath("$.leida").value(true));

        verify(service).marcarComoLeida(10L, CORREO);
    }

    // =========================================
    // PRUEBA 5: NOTIFICACION AJENA O INEXISTENTE
    // =========================================

    @Test
    void debeRetornar404ParaNotificacionAjena()
            throws Exception {

        when(service.marcarComoLeida(99L, CORREO))
                .thenThrow(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Notificacion no encontrada"
                        )
                );

        mockMvc.perform(
                patch("/api/v1/notifications/99/read")
                        .principal(() -> CORREO)
        )
        .andExpect(status().isNotFound());

        verify(service).marcarComoLeida(99L, CORREO);
    }

    // =========================================
    // PRUEBA 6: LISTAR SIN USUARIO
    // =========================================

    @Test
    void debeRechazarListadoSinUsuario()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/notifications")
        )
        .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    // =========================================
    // PRUEBA 7: CONTADOR SIN USUARIO
    // =========================================

    @Test
    void debeRechazarContadorSinUsuario()
            throws Exception {

        mockMvc.perform(
                get("/api/v1/notifications/unread-count")
        )
        .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }

    // =========================================
    // PRUEBA 8: MARCAR LEIDA SIN USUARIO
    // =========================================

    @Test
    void debeRechazarLecturaSinUsuario()
            throws Exception {

        mockMvc.perform(
                patch("/api/v1/notifications/10/read")
        )
        .andExpect(status().isUnauthorized());

        verifyNoInteractions(service);
    }
}