package com.proyectocitas.service;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IdentityMailFailureTest {
    @Test
    void caidaSmtpNoExponeTokenCredencialesNiExcepcion() {
        var service = mock(PasswordRecoveryService.class);
        var mail = mock(MailService.class);
        String sensitive = "contenido-sensible-que-nunca-debe-aparecer-en-logs";
        when(service.issueToken("ana@example.com")).thenReturn(Optional.of(
                new PasswordRecoveryService.TokenDelivery("ana@example.com", sensitive)));
        doThrow(new MailSendException(sensitive)).when(mail).sendPasswordRecovery(any());
        var logger = (Logger) LoggerFactory.getLogger(PasswordRecoveryMailListener.class);
        var events = new ListAppender<ILoggingEvent>();
        events.start();
        logger.addAppender(events);
        try {
            assertDoesNotThrow(() -> new PasswordRecoveryMailListener(service, mail)
                    .onRequest(new PasswordRecoveryRequested("ana@example.com")));
            assertFalse(events.list.isEmpty());
            assertTrue(events.list.stream().noneMatch(event -> event.getFormattedMessage().contains(sensitive)));
            assertTrue(events.list.stream().allMatch(event -> event.getThrowableProxy() == null));
        } finally {
            logger.detachAppender(events);
            events.stop();
        }
    }

    @Test
    void falloAlPersistirNoEnviaEnlacesYSolicitudDesconocidaNoEnviaCorreo() {
        var service = mock(PasswordRecoveryService.class);
        var mail = mock(MailService.class);
        when(service.issueToken("ana@example.com")).thenThrow(new IllegalStateException("falló la transacción"));
        when(service.issueToken("nadie@example.com")).thenReturn(Optional.empty());
        var listener = new PasswordRecoveryMailListener(service, mail);
        assertDoesNotThrow(() -> listener.onRequest(new PasswordRecoveryRequested("ana@example.com")));
        listener.onRequest(new PasswordRecoveryRequested("nadie@example.com"));
        verifyNoInteractions(mail);
    }
}
