package com.proyectocitas.service;

import com.proyectocitas.config.IdentityProperties;
import com.proyectocitas.model.PasswordResetToken;
import com.proyectocitas.repository.PasswordResetTokenRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.security.IdentityRateLimiter;
import com.proyectocitas.security.IdentityTokens;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordRecoveryService {
    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryService.class);
    private final UsuarioRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final Clock clock;
    private final IdentityProperties properties;
    private final IdentityRateLimiter limiter;
    private final ApplicationEventPublisher publisher;
    private final EntityManager entityManager;

    public PasswordRecoveryService(UsuarioRepository users, PasswordResetTokenRepository tokens,
            PasswordEncoder encoder, @Qualifier("identityClock") Clock clock, IdentityProperties properties,
            IdentityRateLimiter limiter, ApplicationEventPublisher publisher, EntityManager entityManager) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.clock = clock;
        this.properties = properties;
        this.limiter = limiter;
        this.publisher = publisher;
        this.entityManager = entityManager;
    }

    public void requestRecovery(String email, String remoteAddress) {
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        limiter.checkRequest("password-forgot", normalized, remoteAddress);
        try {
            publisher.publishEvent(new PasswordRecoveryRequested(normalized));
        } catch (TaskRejectedException ex) {
            logger.warn("No se pudo encolar una solicitud de recuperación");
        }
    }

    @Transactional
    public Optional<TokenDelivery> issueToken(String email) {
        return users.buscarCorreoParaIdentidad(email).filter(user -> user.isEnabled()).map(user -> {
            String raw = IdentityTokens.generate();
            var token = new PasswordResetToken();
            token.setUsuario(user);
            token.setCorreo(user.getCorreo());
            token.setVersionSesion(user.getVersionSesion());
            token.setTokenHash(IdentityTokens.hash(raw));
            token.setCreadoEn(clock.instant());
            token.setExpiraEn(clock.instant().plus(properties.getResetTokenTtl()));
            tokens.save(token);
            return new TokenDelivery(user.getCorreo(), raw);
        });
    }

    @Transactional
    public void resetPassword(String raw, String password, String remoteAddress) {
        limiter.checkTokenAttempt("password-reset", remoteAddress);
        if (!IdentityTokens.hasValidFormat(raw)) throw invalidLink();
        if (password == null || password.isBlank() || password.length() < 8 || password.length() > 64) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña debe tener entre 8 y 64 caracteres");
        }
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña supera el límite de 72 bytes UTF-8");
        }
        String hash = IdentityTokens.hash(raw);
        Long userId = tokens.obtenerUsuarioPorHash(hash).orElseThrow(this::invalidLink);
        var user = users.findById(userId).orElseThrow(this::invalidLink);
        // Refrescar bajo bloqueo también si OSIV ya cargó el usuario durante el filtro JWT.
        entityManager.refresh(user, LockModeType.PESSIMISTIC_WRITE);
        var token = tokens.buscarParaUso(hash).orElseThrow(this::invalidLink);
        if (!user.isEnabled() || token.getUsadoEn() != null || !token.getExpiraEn().isAfter(clock.instant())
                || token.getVersionSesion() != user.getVersionSesion()
                || !token.getCorreo().equalsIgnoreCase(user.getCorreo())) throw invalidLink();
        if (encoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser diferente de la actual");
        }
        user.setContraseña(encoder.encode(password));
        user.setDebeCambiarPassword(false);
        user.setIntentosFallidos(0);
        user.setBloqueadoHasta(null);
        user.setVersionSesion(user.getVersionSesion() + 1);
        token.setUsadoEn(clock.instant());
        users.save(user);
        tokens.save(token);
    }

    private ResponseStatusException invalidLink() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "El enlace no es válido o ya venció. Solicita uno nuevo.");
    }

    /** No genera toString con el token; nunca serializar esta entrega como respuesta HTTP. */
    public static final class TokenDelivery {
        private final String email;
        private final String token;
        public TokenDelivery(String email, String token) { this.email = email; this.token = token; }
        public String email() { return email; }
        public String token() { return token; }
    }
}
