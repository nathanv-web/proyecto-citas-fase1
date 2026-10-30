
package com.proyectocitas.integration;

import com.proyectocitas.model.Notificacion;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.NotificacionRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.CitaReservadaEvent;
import com.proyectocitas.service.MailService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:citanova-notificaciones-tx;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.sql.init.mode=never",
    "jwt.secret=clave-exclusiva-para-pruebas-de-citanova-01234567890123456789",
    "jwt.expiration=3600000"
})
class CitaReservadaTransaccionIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publicadorEventos;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @MockitoBean
    private MailService mailService;

    // =========================================
    // CREAR USUARIO MEDICO DE PRUEBA
    // =========================================

    private Usuario crearUsuarioMedico() {

        Usuario usuario = new Usuario();

        usuario.setNombre("Medico");
        usuario.setApellido("Prueba");
        usuario.setCorreo(
                "medico-" + UUID.randomUUID()
                + "@example.com"
        );
        usuario.setTelefono("55555555");
        usuario.setContraseña("clave-solo-para-pruebas");

        return usuarioRepository.saveAndFlush(usuario);
    }

    // =========================================
    // GENERAR EVENTO DE RESERVA
    // =========================================

    private CitaReservadaEvent crearEvento(
            Usuario medico,
            String correoPaciente) {

        return new CitaReservadaEvent(
                100L,
                medico.getIdUsuario(),
                correoPaciente,
                LocalDate.of(2026, 11, 10),
                LocalTime.of(9, 0)
        );
    }

    // =========================================
    // PRUEBA 1: TRANSACCION CONFIRMADA
    // =========================================

    @Test
    void debeGuardarAvisoYEnviarCorreoDespuesDelCommit() {

        Usuario medico = crearUsuarioMedico();

        String correoPaciente =
                "paciente-" + UUID.randomUUID()
                + "@example.com";

        CitaReservadaEvent evento =
                crearEvento(medico, correoPaciente);

        TransactionTemplate transaccion =
                new TransactionTemplate(transactionManager);

        // Ejecutar una transaccion real.
        transaccion.executeWithoutResult(status -> {

            publicadorEventos.publishEvent(evento);

            // El correo no debe enviarse
            // antes de confirmar la transaccion.
            verify(mailService, never()).send(
                    anyString(),
                    anyString(),
                    anyString()
            );
        });

        // La transaccion ya fue confirmada.

        List<Notificacion> avisos =
                notificacionRepository
                    .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                            medico.getIdUsuario()
                    );

        assertEquals(1, avisos.size());

        Notificacion aviso = avisos.get(0);

        assertEquals(
                "Nueva cita reservada",
                aviso.getTitulo()
        );

        assertEquals(
                Notificacion.Tipo.CITA_RESERVADA,
                aviso.getTipo()
        );

        assertFalse(aviso.isLeida());

        assertEquals(
                medico.getIdUsuario(),
                aviso.getDestinatario().getIdUsuario()
        );

        // El envio es asincrono.
        // Esperamos hasta 5 segundos.
        verify(mailService, timeout(5000)).send(
                eq(correoPaciente),
                contains("CitaNova"),
                contains("100")
        );
    }

    // =========================================
    // PRUEBA 2: TRANSACCION REVERTIDA
    // =========================================

    @Test
    void noDebeGuardarAvisoNiEnviarCorreoSiHayRollback() {

        Usuario medico = crearUsuarioMedico();

        String correoPaciente =
                "paciente-" + UUID.randomUUID()
                + "@example.com";

        CitaReservadaEvent evento =
                crearEvento(medico, correoPaciente);

        TransactionTemplate transaccion =
                new TransactionTemplate(transactionManager);

        transaccion.executeWithoutResult(status -> {

            publicadorEventos.publishEvent(evento);

            // Simulamos que la operacion fallo.
            status.setRollbackOnly();
        });

        // La transaccion fue revertida.

        List<Notificacion> avisos =
                notificacionRepository
                    .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                            medico.getIdUsuario()
                    );

        assertTrue(
                avisos.isEmpty(),
                "No debe guardarse un aviso de reserva fallida"
        );

        // Comprobar que tampoco se solicito
        // el envio del correo.
        verify(mailService, after(700).never()).send(
                anyString(),
                anyString(),
                anyString()
        );
    }
}
