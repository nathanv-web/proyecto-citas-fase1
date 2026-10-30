package com.proyectocitas.service;

import com.proyectocitas.model.Notificacion;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.NotificacionRepository;
import com.proyectocitas.repository.UsuarioRepository;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class CitaReservadaListenerTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MailService mailService;

    @InjectMocks
    private CitaReservadaListener listener;

    private CitaReservadaEvent evento;

    @BeforeEach
    void setUp() {

        evento = new CitaReservadaEvent(
                100L,
                2L,
                "paciente@correo.com",
                LocalDate.of(2026, 11, 10),
                LocalTime.of(9, 0)
        );
    }

    // =========================================
    // PRUEBA 1: GUARDAR AVISO AL MEDICO
    // =========================================

    @Test
    void debeGuardarNotificacionParaElMedico() {

        Usuario medico = new Usuario();
        medico.setIdUsuario(2L);

        when(usuarioRepository.getReferenceById(2L))
                .thenReturn(medico);

        listener.guardarNotificacion(evento);

        ArgumentCaptor<Notificacion> captor =
                ArgumentCaptor.forClass(
                        Notificacion.class
                );

        verify(notificacionRepository)
                .save(captor.capture());

        Notificacion guardada = captor.getValue();

        assertSame(medico, guardada.getDestinatario());

        assertEquals(
                "Nueva cita reservada",
                guardada.getTitulo()
        );

        assertEquals(
                Notificacion.Tipo.CITA_RESERVADA,
                guardada.getTipo()
        );

        assertFalse(guardada.isLeida());

        assertTrue(
                guardada.getMensaje().contains("2026-11-10")
        );

        // Guardar el aviso no debe enviar el correo
        verifyNoInteractions(mailService);
    }

    // =========================================
    // PRUEBA 2: ENVIAR CONFIRMACION
    // =========================================

    @Test
    void debePrepararCorreoDeConfirmacion() {

        listener.enviarConfirmacion(evento);

        verify(mailService).send(
                eq("paciente@correo.com"),
                contains("CitaNova"),
                contains("100")
        );
    }

    // =========================================
    // PRUEBA 3: ERROR EN SMTP
    // =========================================

    @Test
    void noDebePropagarErrorDeCorreo() {

        doThrow(new RuntimeException("SMTP no disponible"))
                .when(mailService)
                .send(
                        anyString(),
                        anyString(),
                        anyString()
                );

        assertDoesNotThrow(
                () -> listener.enviarConfirmacion(evento)
        );

        verify(mailService).send(
                eq("paciente@correo.com"),
                anyString(),
                anyString()
        );
    }
}