package com.proyectocitas.service;

import com.proyectocitas.config.IdentityProperties;
import com.proyectocitas.model.EmailVerificationToken;
import com.proyectocitas.repository.EmailVerificationTokenRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.security.IdentityRateLimiter;
import com.proyectocitas.security.IdentityTokens;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailVerificationService {
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationService.class);
    private final UsuarioRepository users;
    private final EmailVerificationTokenRepository tokens;
    private final Clock clock;
    private final IdentityProperties properties;
    private final IdentityRateLimiter limiter;
    private final ApplicationEventPublisher publisher;
    private final EntityManager entityManager;

    public EmailVerificationService(UsuarioRepository users, EmailVerificationTokenRepository tokens,
            @Qualifier("identityClock") Clock clock, IdentityProperties properties,
            IdentityRateLimiter limiter, ApplicationEventPublisher publisher, EntityManager entityManager) {
        this.users = users;
        this.tokens = tokens;
        this.clock = clock;
        this.properties = properties;
        this.limiter = limiter;
        this.publisher = publisher;
        this.entityManager = entityManager;
    }

    public void requestVerification(String email, String remoteAddress) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        limiter.checkRequest("email-request", normalized, remoteAddress);
        requestAfterRegistration(normalized);
    }

    public void requestAfterRegistration(String email) {
        try {
            publisher.publishEvent(new EmailVerificationRequested(email));
        } catch (TaskRejectedException ex) {
            logger.warn("No se pudo encolar una solicitud de verificación de correo");
        }
    }

    @Transactional
    public Optional<TokenDelivery> issueToken(String email) {
        return users.buscarCorreoParaIdentidad(email)
                .filter(user -> user.isEnabled() && !user.isCorreoVerificado()).map(user -> {
                    String raw = IdentityTokens.generate();
                    var token = new EmailVerificationToken();
                    token.setUsuario(user);
                    token.setCorreo(user.getCorreo());
                    token.setTokenHash(IdentityTokens.hash(raw));
                    token.setCreadoEn(clock.instant());
                    token.setExpiraEn(clock.instant().plus(properties.getVerificationTokenTtl()));
                    tokens.save(token);
                    return new TokenDelivery(user.getCorreo(), raw);
                });
    }

    @Transactional
    public void verify(String raw, String remoteAddress) {
        limiter.checkTokenAttempt("email-verify", remoteAddress);
        if (!IdentityTokens.hasValidFormat(raw)) throw invalidLink();
        String hash = IdentityTokens.hash(raw);
        Long userId = tokens.obtenerUsuarioPorHash(hash).orElseThrow(this::invalidLink);
        var user = users.findById(userId).orElseThrow(this::invalidLink);
        entityManager.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        var token = tokens.buscarParaUso(hash).orElseThrow(this::invalidLink);
        if (!user.isEnabled() || user.isCorreoVerificado() || token.getUsadoEn() != null
                || !token.getExpiraEn().isAfter(clock.instant())
                || !token.getCorreo().equalsIgnoreCase(user.getCorreo())) throw invalidLink();
        user.setCorreoVerificado(true);
        token.setUsadoEn(clock.instant());
        users.save(user);
        tokens.save(token);
    }

    private ResponseStatusException invalidLink() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "El enlace de verificación no es válido o ya venció. Solicita uno nuevo.");
    }

    public static final class TokenDelivery {
        private final String email;
        private final String token;
        public TokenDelivery(String email, String token) { this.email = email; this.token = token; }
        public String email() { return email; }
        public String token() { return token; }
    }
}
