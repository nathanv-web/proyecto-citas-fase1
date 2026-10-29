package com.proyectocitas.service;

import com.proyectocitas.config.IdentityProperties;
import com.proyectocitas.model.PasswordResetToken;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.PasswordResetTokenRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.security.IdentityRateLimiter;
import com.proyectocitas.security.JwtService;
import jakarta.persistence.EntityManager;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PasswordRecoveryServiceTest {
    private final Instant now = Instant.parse("2026-10-28T12:00:00Z");
    private final Clock clock = Clock.fixed(now, ZoneOffset.UTC);
    private final UsuarioRepository users = mock(UsuarioRepository.class);
    private final PasswordResetTokenRepository tokens = mock(PasswordResetTokenRepository.class);
    private final EntityManager entityManager = mock(EntityManager.class);
    private final ApplicationEventPublisher publisher = mock(ApplicationEventPublisher.class);
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final IdentityProperties properties = new IdentityProperties();
    private PasswordRecoveryService service;
    private Usuario user;
    private PasswordResetToken saved;

    @BeforeEach
    void setup() {
        user = new Usuario();
        user.setIdUsuario(7L);
        user.setCorreo("ana@example.com");
        user.setActivo(true);
        user.setContraseña(encoder.encode("Anterior123"));
        service = new PasswordRecoveryService(users, tokens, encoder, clock, properties,
                new IdentityRateLimiter(clock, properties), publisher, entityManager);
        when(users.buscarCorreoParaIdentidad(anyString())).thenReturn(Optional.of(user));
        when(users.findById(7L)).thenReturn(Optional.of(user));
        when(tokens.save(any())).thenAnswer(invocation -> { saved = invocation.getArgument(0); return saved; });
    }

    private String issue() {
        var delivery = service.issueToken("ana@example.com").orElseThrow();
        String token = delivery.token();
        when(tokens.obtenerUsuarioPorHash(saved.getTokenHash())).thenReturn(Optional.of(7L));
        when(tokens.buscarParaUso(saved.getTokenHash())).thenAnswer(invocation -> Optional.of(saved));
        return token;
    }

    @Test
    void emiteTokenAleatorioYGuardaSoloHashConCaducidad() {
        String token = issue();
        assertTrue(token.matches("[A-Za-z0-9_-]{43}"));
        assertEquals(64, saved.getTokenHash().length());
        assertNotEquals(token, saved.getTokenHash());
        assertEquals(now.plusSeconds(1800), saved.getExpiraEn());
        assertNull(saved.getUsadoEn());
        assertEquals("ana@example.com", saved.getCorreo());
        verifyNoInteractions(publisher);
    }

    @Test
    void noGeneraTokenParaCuentaDesconocidaODesactivada() {
        when(users.buscarCorreoParaIdentidad("nadie@example.com")).thenReturn(Optional.empty());
        assertTrue(service.issueToken("nadie@example.com").isEmpty());
        user.setActivo(false);
        assertTrue(service.issueToken("ana@example.com").isEmpty());
        verify(tokens, never()).save(any());
    }

    @Test
    void restableceUnaSolaVezDesbloqueaEInvalidaJwtAnterior() {
        JwtService jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secret", "clave-de-prueba-sin-uso-real-12345678901234567890");
        ReflectionTestUtils.setField(jwt, "expiration", 3_600_000L);
        String oldJwt = jwt.generateToken(user);
        String token = issue();
        user.setDebeCambiarPassword(true);
        user.setIntentosFallidos(5);
        user.setBloqueadoHasta(java.time.LocalDateTime.now().plusHours(1));
        service.resetPassword(token, "NuevaClave123", "127.0.0.1");
        assertTrue(encoder.matches("NuevaClave123", user.getPassword()));
        assertFalse(encoder.matches("Anterior123", user.getPassword()));
        assertEquals(1, user.getVersionSesion());
        assertFalse(user.isDebeCambiarPassword());
        assertEquals(0, user.getIntentosFallidos());
        assertNull(user.getBloqueadoHasta());
        assertEquals(now, saved.getUsadoEn());
        assertFalse(jwt.isTokenValid(oldJwt, user));
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(token, "OtraClave123", "127.0.0.1"));
    }

    @Test
    void rechazaTokenVencidoSinCambiarClaveNiMarcarUso() {
        String token = issue();
        saved.setExpiraEn(now);
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(token, "NuevaClave123", "127.0.0.1"));
        assertTrue(encoder.matches("Anterior123", user.getPassword()));
        assertNull(saved.getUsadoEn());
    }

    @Test
    void rechazaTokenSiLaClaveOCorreoYaCambioOSeDesactivoCuenta() {
        String token = issue();
        user.setVersionSesion(2);
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(token, "NuevaClave123", "127.0.0.1"));
        user.setVersionSesion(0);
        user.setCorreo("otra@example.com");
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(token, "NuevaClave123", "127.0.0.1"));
        user.setCorreo("ana@example.com");
        user.setActivo(false);
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(token, "NuevaClave123", "127.0.0.1"));
        assertNull(saved.getUsadoEn());
    }

    @Test
    void rechazaTokenInvalidoClaveCortaYClaveQueSupera72BytesSinConsumirToken() {
        assertThrows(ResponseStatusException.class, () -> service.resetPassword("invalido", "NuevaClave123", "127.0.0.1"));
        String token = issue();
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(token, "corta", "127.0.0.1"));
        assertThrows(ResponseStatusException.class, () -> service.resetPassword(token, "á".repeat(40), "127.0.0.1"));
        assertNull(saved.getUsadoEn());
        service.resetPassword(token, "NuevaClave123", "127.0.0.1");
        assertNotNull(saved.getUsadoEn());
    }

    @Test
    void solicitudSoloEncolaTrabajoYLimitaIgualUnCorreoDesconocido() {
        for (int i = 0; i < 3; i++) service.requestRecovery(" Nadie@Example.com ", "127.0.0.1");
        verifyNoInteractions(users, tokens);
        assertEquals(429, assertThrows(ResponseStatusException.class,
                () -> service.requestRecovery("nadie@example.com", "127.0.0.1")).getStatusCode().value());
        verify(publisher, times(3)).publishEvent(any(PasswordRecoveryRequested.class));
    }

    @Test
    void colaLlenaNoRevelaSiExisteLaCuentaNiEjecutaSmtpEnElHiloHttp() {
        doThrow(new org.springframework.core.task.TaskRejectedException("cola llena"))
                .when(publisher).publishEvent(any(PasswordRecoveryRequested.class));
        assertDoesNotThrow(() -> service.requestRecovery("nadie@example.com", "127.0.0.1"));
        verifyNoInteractions(users, tokens);
    }
}
