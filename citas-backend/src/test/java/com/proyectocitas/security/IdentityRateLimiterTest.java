package com.proyectocitas.security;

import com.proyectocitas.config.IdentityProperties;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IdentityRateLimiterTest {
    @Test
    void ipBloqueadaNoAgotaLaCapacidadParaOtrosUsuarios() {
        var properties = new IdentityProperties();
        properties.setIpRequestLimit(1);
        properties.setMaxRateLimitEntries(4);
        var limiter = new IdentityRateLimiter(Clock.systemUTC(), properties);
        limiter.checkRequest("forgot", "primera@example.com", "ip-bloqueada");

        for (int i = 0; i < 5; i++) {
            String email = "rechazada-" + i + "@example.com";
            var error = assertThrows(ResponseStatusException.class,
                    () -> limiter.checkRequest("forgot", email, "ip-bloqueada"));
            assertEquals(429, error.getStatusCode().value());
        }

        assertDoesNotThrow(() -> limiter.checkRequest("forgot", "legitima@example.com", "otra-ip"));
    }

    @Test
    void normalizaCorreoLimitaIPsYLiberaLaVentana() {
        var clock = mock(Clock.class);
        var now = Instant.parse("2026-10-28T00:00:00Z");
        when(clock.instant()).thenReturn(now);
        var properties = new IdentityProperties();
        properties.setEmailRequestLimit(1);
        properties.setIpRequestLimit(2);
        var limiter = new IdentityRateLimiter(clock, properties);
        limiter.checkRequest("forgot", "Ana@example.com", "ip-1");
        assertThrows(ResponseStatusException.class, () -> limiter.checkRequest("forgot", " ana@EXAMPLE.com ", "ip-2"));
        limiter.checkRequest("forgot", "otra@example.com", "ip-1");
        assertThrows(ResponseStatusException.class, () -> limiter.checkRequest("forgot", "tercera@example.com", "ip-1"));
        when(clock.instant()).thenReturn(now.plus(properties.getRequestWindow()));
        assertDoesNotThrow(() -> limiter.checkRequest("forgot", "ana@example.com", "ip-1"));
    }

    @Test
    void capacidadAcotadaRechazaClavesNuevasSinEliminarLimitesVigentes() {
        var clock = Clock.systemUTC();
        var properties = new IdentityProperties();
        properties.setMaxRateLimitEntries(2);
        properties.setEmailRequestLimit(1);
        var limiter = new IdentityRateLimiter(clock, properties);
        limiter.checkRequest("forgot", "ana@example.com", "ip-1");
        assertThrows(ResponseStatusException.class, () -> limiter.checkRequest("forgot", "otra@example.com", "ip-2"));
        assertThrows(ResponseStatusException.class, () -> limiter.checkRequest("forgot", "ana@example.com", "ip-1"));
    }
}
