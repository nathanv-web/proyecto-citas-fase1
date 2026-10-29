package com.proyectocitas.integration;

import com.jayway.jsonpath.JsonPath;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.PasswordResetTokenRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.security.JwtService;
import com.proyectocitas.service.PasswordRecoveryService;
import com.proyectocitas.service.EmailVerificationService;
import com.proyectocitas.service.AutenticacionService;
import com.proyectocitas.dto.request.PasswordChangeRequestDTO;
import jakarta.persistence.EntityManager;
import com.proyectocitas.support.LocalSmtpServer;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:identity-recovery;MODE=MariaDB;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
    "spring.datasource.driver-class-name=org.h2.Driver", "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false", "spring.sql.init.mode=never",
    "spring.mail.host=127.0.0.1", "spring.mail.username=", "spring.mail.password=",
    "spring.mail.properties.mail.smtp.auth=false", "spring.mail.properties.mail.smtp.starttls.enable=false",
    "spring.mail.properties.mail.smtp.ssl.enable=false",
    "jwt.secret=solo-pruebas-aisladas-sin-valor-productivo-12345678901234567890", "jwt.expiration=3600000",
    "app.identity.ip-request-limit=100", "app.identity.frontend-base-url=http://localhost:4200",
    "spring.jpa.properties.hibernate.jdbc.time_zone=UTC"
})
@AutoConfigureMockMvc
class PasswordRecoveryIntegrationTest {
    private static final LocalSmtpServer SMTP;
    static {
        try { SMTP = new LocalSmtpServer(); }
        catch (Exception ex) { throw new ExceptionInInitializerError(ex); }
    }
    @DynamicPropertySource
    static void smtp(DynamicPropertyRegistry registry) { registry.add("spring.mail.port", SMTP::port); }
    @AfterAll
    static void closeSmtp() throws Exception { SMTP.close(); }

    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository users;
    @Autowired PasswordResetTokenRepository tokens;
    @Autowired PasswordRecoveryService service;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtService jwt;
    @Autowired EmailVerificationService verification;
    @Autowired AutenticacionService authentication;
    @Autowired EntityManager entityManager;
    @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;

    private Usuario user() {
        var user = new Usuario();
        user.setNombre("Ana");
        user.setApellido("Lopez");
        user.setCorreo("test-" + UUID.randomUUID() + "@example.com");
        user.setTelefono("55554444");
        user.setContraseña(encoder.encode("Anterior123"));
        return users.saveAndFlush(user);
    }

    @Test
    void correoSmtpRealResetLoginYRevocacionDeSesionFuncionanJuntos() throws Exception {
        var user = user();
        String previousJwt = jwt.generateToken(user);
        mvc.perform(post("/api/v1/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + user.getCorreo() + "\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").doesNotExist());
        var mail = SMTP.awaitMessage(user.getCorreo(), Duration.ofSeconds(8));
        assertEquals("CitaNova: restablecer contraseña", mail.getSubject());
        var matcher = Pattern.compile("/reset-password\\?token=([A-Za-z0-9_-]{43})").matcher((String) mail.getContent());
        assertTrue(matcher.find(), "El correo debe incluir el enlace de recuperación");
        String raw = matcher.group(1);
        var stored = tokens.findAll().stream().filter(token -> token.getTokenHash()
                .equals(com.proyectocitas.security.IdentityTokens.hash(raw))).findFirst().orElseThrow();
        assertNotEquals(raw, stored.getTokenHash());
        reset(raw, "NuevaClave123", 200);
        reset(raw, "OtraClave123", 400);
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + previousJwt))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + user.getCorreo() + "\",\"password\":\"Anterior123\"}"))
                .andExpect(status().isUnauthorized());
        String login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + user.getCorreo() + "\",\"password\":\"NuevaClave123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String newJwt = JsonPath.read(login, "$.token");
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + newJwt)).andExpect(status().isOk());
    }

    @Test
    void tokensExpiradosYDesconocidosSeRechazanEnLaApi() throws Exception {
        var user = user();
        String raw = service.issueToken(user.getCorreo()).orElseThrow().token();
        var token = tokens.findAll().stream().filter(t -> t.getCorreo().equals(user.getCorreo())).findFirst().orElseThrow();
        token.setExpiraEn(Instant.now().minusSeconds(1));
        tokens.saveAndFlush(token);
        reset(raw, "NuevaClave123", 400);
        reset("B".repeat(43), "NuevaClave123", 400);
        assertTrue(encoder.matches("Anterior123", users.findById(user.getIdUsuario()).orElseThrow().getPassword()));
    }

    @Test
    void dosPeticionesSimultaneasConElMismoTokenSoloCambianUnaVez() throws Exception {
        var user = user();
        String raw = service.issueToken(user.getCorreo()).orElseThrow().token();
        assertEquals(List.of(200, 400), race(raw, raw).stream().sorted().toList());
        assertEquals(1, users.findById(user.getIdUsuario()).orElseThrow().getVersionSesion());
    }

    @Test
    void dosTokensDeLaMismaCuentaTambienSeSerializanPorUsuario() throws Exception {
        var user = user();
        String first = service.issueToken(user.getCorreo()).orElseThrow().token();
        String second = service.issueToken(user.getCorreo()).orElseThrow().token();
        assertEquals(List.of(200, 400), race(first, second).stream().sorted().toList());
        assertEquals(1, users.findById(user.getIdUsuario()).orElseThrow().getVersionSesion());
    }

    @Test
    void registroEnviaVerificacionYLosTokensNoSeIntercambianEntreFlujos() throws Exception {
        String email = "registro-" + UUID.randomUUID() + "@example.com";
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"apellido\":\"Lopez\",\"correo\":\"" + email
                        + "\",\"telefono\":\"55554444\",\"contrasena\":\"Anterior123\"}"))
                .andExpect(status().isCreated());
        var mail = SMTP.awaitMessage(email, Duration.ofSeconds(8));
        assertEquals("CitaNova: verifica tu correo", mail.getSubject());
        var matcher = Pattern.compile("/verify-email\\?token=([A-Za-z0-9_-]{43})").matcher((String) mail.getContent());
        assertTrue(matcher.find());
        String verifyToken = matcher.group(1);
        reset(verifyToken, "NuevaClave123", 400);
        mvc.perform(post("/api/v1/auth/email/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + verifyToken + "\"}"))
                .andExpect(status().isOk());
        assertTrue(users.findByCorreo(email).orElseThrow().isCorreoVerificado());
        mvc.perform(post("/api/v1/auth/email/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + verifyToken + "\"}"))
                .andExpect(status().isBadRequest());
        String passwordToken = service.issueToken(email).orElseThrow().token();
        mvc.perform(post("/api/v1/auth/email/verify").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + passwordToken + "\"}"))
                .andExpect(status().isBadRequest());
        String login = mvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"" + email + "\",\"password\":\"Anterior123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.correoVerificado").value(true))
                .andReturn().getResponse().getContentAsString();
        String bearer = JsonPath.read(login, "$.token");
        mvc.perform(get("/api/v1/me").header("Authorization", "Bearer " + bearer))
                .andExpect(status().isOk()).andExpect(jsonPath("$.usuario.correoVerificado").value(true));
    }

    @Test
    void solicitudDeVerificacionEsPublicaYGenerica() throws Exception {
        mvc.perform(post("/api/v1/auth/email/request").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nadie@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    void unaEdicionAntiguaNoPuedeRestaurarLaClaveNiVersionPreviasAlReset() {
        var stale = user();
        String raw = service.issueToken(stale.getCorreo()).orElseThrow().token();
        service.resetPassword(raw, "NuevaClave123", "stale-update-test");
        stale.setTelefono("55550000");
        assertThrows(org.springframework.dao.OptimisticLockingFailureException.class,
                () -> users.saveAndFlush(stale));
        var actual = users.findById(stale.getIdUsuario()).orElseThrow();
        assertTrue(encoder.matches("NuevaClave123", actual.getPassword()));
        assertEquals(1, actual.getVersionSesion());
    }

    @Test
    void cambioAutenticadoNoAceptaClaveAnteriorPrecargadaAntesDeUnReset() {
        var user = user();
        String raw = service.issueToken(user.getCorreo()).orElseThrow().token();
        var transaction = new org.springframework.transaction.support.TransactionTemplate(transactionManager);
        transaction.executeWithoutResult(status -> {
            // Reproduce la instancia de usuario precargada por JWT/OSIV.
            entityManager.find(Usuario.class, user.getIdUsuario());
            try {
                CompletableFuture.runAsync(() -> service.resetPassword(raw, "NuevaClave123", "cached-user-test"))
                        .get(15, TimeUnit.SECONDS);
            } catch (Exception ex) {
                throw new IllegalStateException("El reset concurrente de prueba no terminó", ex);
            }
            var request = new PasswordChangeRequestDTO();
            request.setActual("Anterior123");
            request.setNueva("OtraClave123");
            assertEquals(400, assertThrows(ResponseStatusException.class,
                    () -> authentication.cambiarPassword(user.getCorreo(), request)).getStatusCode().value());
        });
        var actual = users.findById(user.getIdUsuario()).orElseThrow();
        assertTrue(encoder.matches("NuevaClave123", actual.getPassword()));
        assertEquals(1, actual.getVersionSesion());
    }

    @Test
    void loginNoAceptaClaveRevocadaAunqueElContextoYaTengaElUsuario() {
        var user = user();
        String raw = service.issueToken(user.getCorreo()).orElseThrow().token();
        new org.springframework.transaction.support.TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            entityManager.find(Usuario.class, user.getIdUsuario());
            try {
                CompletableFuture.runAsync(() -> service.resetPassword(raw, "NuevaClave123", "cached-login-test"))
                        .get(15, TimeUnit.SECONDS);
            } catch (Exception ex) {
                throw new IllegalStateException("El reset concurrente de prueba no terminó", ex);
            }
            assertEquals(401, assertThrows(ResponseStatusException.class,
                    () -> authentication.autenticar(user.getCorreo(), "Anterior123")).getStatusCode().value());
        });
        assertTrue(encoder.matches("NuevaClave123", users.findById(user.getIdUsuario()).orElseThrow().getPassword()));
    }

    private List<Integer> race(String first, String second) throws Exception {
        var start = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            var a = executor.submit(() -> attempt(start, first, "NuevaClave123"));
            var b = executor.submit(() -> attempt(start, second, "OtraClave123"));
            start.countDown();
            return List.of(a.get(15, TimeUnit.SECONDS), b.get(15, TimeUnit.SECONDS));
        }
    }

    private int attempt(CountDownLatch start, String raw, String password) throws Exception {
        start.await();
        try { service.resetPassword(raw, password, "concurrency-test"); return 200; }
        catch (ResponseStatusException ex) { return ex.getStatusCode().value(); }
    }

    private void reset(String raw, String password, int status) throws Exception {
        mvc.perform(post("/api/v1/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + raw + "\",\"nueva\":\"" + password + "\"}"))
                .andExpect(status().is(status));
    }
}
