package com.proyectocitas.service;

import com.proyectocitas.dto.response.NotificacionResponseDTO;
import com.proyectocitas.model.Notificacion;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.NotificacionRepository;
import com.proyectocitas.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private NotificacionService service;

    private Usuario usuario;

    private static final String CORREO =
            "paciente@correo.com";

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setCorreo(CORREO);
    }

    // Simular usuario autenticado
    private void prepararUsuario() {
        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.of(usuario));
    }

    // Crear notificacion de prueba
    private Notificacion crearNotificacion(
            Long id,
            boolean leida) {

        Notificacion notificacion = new Notificacion();

        ReflectionTestUtils.setField(
                notificacion,
                "idNotificacion",
                id
        );

        notificacion.setDestinatario(usuario);
        notificacion.setTitulo("Nueva cita");
        notificacion.setMensaje("Tienes una cita reservada");
        notificacion.setTipo(
                Notificacion.Tipo.CITA_RESERVADA
        );
        notificacion.setLeida(leida);

        notificacion.prePersist();

        return notificacion;
    }

    // =========================================
    // PRUEBA 1: LISTAR MIS NOTIFICACIONES
    // =========================================

    @Test
    void debeListarMisNotificaciones() {

        prepararUsuario();

        Notificacion aviso =
                crearNotificacion(10L, false);

        when(notificacionRepository
                .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                        1L
                ))
                .thenReturn(List.of(aviso));

        List<NotificacionResponseDTO> resultado =
                service.listarMias(CORREO);

        assertEquals(1, resultado.size());
        assertEquals(10L, resultado.get(0).idNotificacion());
        assertEquals("Nueva cita", resultado.get(0).titulo());
        assertFalse(resultado.get(0).leida());

        verify(notificacionRepository)
                .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                        1L
                );
    }

    // =========================================
    // PRUEBA 2: LISTA VACIA
    // =========================================

    @Test
    void debeRetornarListaVacia() {

        prepararUsuario();

        when(notificacionRepository
                .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                        1L
                ))
                .thenReturn(List.of());

        List<NotificacionResponseDTO> resultado =
                service.listarMias(CORREO);

        assertTrue(resultado.isEmpty());
    }

    // =========================================
    // PRUEBA 3: MARCAR COMO LEIDA
    // =========================================

    @Test
    void debeMarcarNotificacionPropiaComoLeida() {

        prepararUsuario();

        Notificacion aviso =
                crearNotificacion(10L, false);

        when(notificacionRepository
                .findByIdNotificacionAndDestinatario_IdUsuario(
                        10L, 1L
                ))
                .thenReturn(Optional.of(aviso));

        NotificacionResponseDTO resultado =
                service.marcarComoLeida(10L, CORREO);

        assertTrue(resultado.leida());
        assertTrue(aviso.isLeida());

        verify(notificacionRepository).save(aviso);
    }

    // =========================================
    // PRUEBA 4: NO GUARDAR SI YA ESTA LEIDA
    // =========================================

    @Test
    void noDebeGuardarNuevamenteSiYaEstaLeida() {

        prepararUsuario();

        Notificacion aviso =
                crearNotificacion(10L, true);

        when(notificacionRepository
                .findByIdNotificacionAndDestinatario_IdUsuario(
                        10L, 1L
                ))
                .thenReturn(Optional.of(aviso));

        NotificacionResponseDTO resultado =
                service.marcarComoLeida(10L, CORREO);

        assertTrue(resultado.leida());

        verify(notificacionRepository, never())
                .save(any(Notificacion.class));
    }

    // =========================================
    // PRUEBA 5: NO MODIFICAR AVISOS AJENOS
    // =========================================

    @Test
    void noDebePermitirModificarNotificacionAjena() {

        prepararUsuario();

        when(notificacionRepository
                .findByIdNotificacionAndDestinatario_IdUsuario(
                        99L, 1L
                ))
                .thenReturn(Optional.empty());

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.marcarComoLeida(
                                99L, CORREO
                        )
                );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatusCode()
        );

        verify(notificacionRepository, never())
                .save(any(Notificacion.class));
    }

    // =========================================
    // PRUEBA 6: CONTAR NO LEIDAS
    // =========================================

    @Test
    void debeContarNotificacionesNoLeidas() {

        prepararUsuario();

        when(notificacionRepository
                .countByDestinatario_IdUsuarioAndLeidaFalse(
                        1L
                ))
                .thenReturn(3L);

        long cantidad = service.contarNoLeidas(CORREO);

        assertEquals(3L, cantidad);
    }

    // =========================================
    // PRUEBA 7: USUARIO NO EXISTE
    // =========================================

    @Test
    void debeRechazarUsuarioNoEncontrado() {

        when(usuarioRepository.findByCorreo(CORREO))
                .thenReturn(Optional.empty());

        ResponseStatusException error =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.listarMias(CORREO)
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                error.getStatusCode()
        );
    }
}