package com.proyectocitas.security;

import com.proyectocitas.config.IdentityProperties;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/** Límite por instancia, acotado en memoria e independiente de la existencia de cuentas. */
@Component
public class IdentityRateLimiter {
    private final Clock clock;
    private final IdentityProperties properties;
    private final Map<String, Window> windows = new HashMap<>();

    public IdentityRateLimiter(@Qualifier("identityClock") Clock clock, IdentityProperties properties) {
        this.clock = clock;
        this.properties = properties;
    }

    public synchronized void checkRequest(String operation, String email, String remoteAddress) {
        Instant now = clock.instant();
        windows.values().removeIf(window -> !window.expires.isAfter(now));
        if (!allow(operation + ":ip:" + IdentityTokens.hash(remoteAddress), properties.getIpRequestLimit(), now)) {
            throw limited();
        }
        boolean account = allow(operation + ":email:" + IdentityTokens.hash(email.trim().toLowerCase(Locale.ROOT)),
                properties.getEmailRequestLimit(), now);
        if (!account) throw limited();
    }

    public synchronized void checkTokenAttempt(String operation, String remoteAddress) {
        Instant now = clock.instant();
        windows.values().removeIf(window -> !window.expires.isAfter(now));
        if (!allow(operation + ":ip:" + IdentityTokens.hash(remoteAddress), properties.getIpRequestLimit(), now)) throw limited();
    }

    private boolean allow(String key, int limit, Instant now) {
        Window window = windows.get(key);
        if (window == null) {
            if (windows.size() >= properties.getMaxRateLimitEntries()) return false;
            window = new Window(now.plus(properties.getRequestWindow()));
            windows.put(key, window);
        }
        if (window.requests >= limit) return false;
        window.requests++;
        return true;
    }

    private ResponseStatusException limited() {
        return new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "Se alcanzó el límite de solicitudes. Intenta nuevamente más tarde.");
    }

    private static final class Window {
        private final Instant expires;
        private int requests;
        private Window(Instant expires) { this.expires = expires; }
    }
}
