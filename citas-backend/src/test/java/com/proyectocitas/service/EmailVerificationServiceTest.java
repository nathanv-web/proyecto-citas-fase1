package com.proyectocitas.service;

import com.proyectocitas.config.IdentityProperties;
import com.proyectocitas.model.EmailVerificationToken;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.EmailVerificationTokenRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.security.IdentityRateLimiter;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmailVerificationServiceTest {
    @Test
    void emiteTokenSeparadoVerificaCorreoYNoPermiteReutilizarlo() {
        var now = Instant.parse("2026-10-28T00:00:00Z");
        var clock = Clock.fixed(now, ZoneOffset.UTC);
        var properties = new IdentityProperties();
        var users = mock(UsuarioRepository.class);
        var tokens = mock(EmailVerificationTokenRepository.class);
        var user = new Usuario();
        user.setIdUsuario(9L);
        user.setCorreo("ana@example.com");
        when(users.buscarCorreoParaIdentidad(user.getCorreo())).thenReturn(Optional.of(user));
        when(users.findById(9L)).thenReturn(Optional.of(user));
        var saved = new java.util.concurrent.atomic.AtomicReference<EmailVerificationToken>();
        when(tokens.save(any())).thenAnswer(call -> { saved.set(call.getArgument(0)); return saved.get(); });
        var service = new EmailVerificationService(users, tokens, clock, properties,
                new IdentityRateLimiter(clock, properties), mock(ApplicationEventPublisher.class), mock(EntityManager.class));
        String raw = service.issueToken(user.getCorreo()).orElseThrow().token();
        var token = saved.get();
        assertNotEquals(raw, token.getTokenHash());
        assertEquals(now.plusSeconds(86400), token.getExpiraEn());
        when(tokens.obtenerUsuarioPorHash(token.getTokenHash())).thenReturn(Optional.of(9L));
        when(tokens.buscarParaUso(token.getTokenHash())).thenReturn(Optional.of(token));
        service.verify(raw, "127.0.0.1");
        assertTrue(user.isCorreoVerificado());
        assertEquals(now, token.getUsadoEn());
        assertThrows(ResponseStatusException.class, () -> service.verify(raw, "127.0.0.1"));
        assertTrue(service.issueToken(user.getCorreo()).isEmpty());
    }
}
