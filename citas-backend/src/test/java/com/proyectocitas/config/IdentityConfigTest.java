package com.proyectocitas.config;

import jakarta.validation.Validation;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mock.env.MockEnvironment;
import static org.junit.jupiter.api.Assertions.*;

class IdentityConfigTest {
    @Test
    void entornoConfiguraSmtpSinCambiarApplicationPropertiesYLimitaTiempos() {
        var environment = new MockEnvironment()
                .withProperty("SMTP_HOST", "smtp.example.test")
                .withProperty("SMTP_PORT", "2525")
                .withProperty("SMTP_USERNAME", "usuario-de-prueba")
                .withProperty("SMTP_PASSWORD", "clave-ficticia-de-prueba")
                .withProperty("SMTP_STARTTLS", "true");
        var sender = (JavaMailSenderImpl) new IdentityConfig().identityMailSender(environment);
        assertEquals("smtp.example.test", sender.getHost());
        assertEquals(2525, sender.getPort());
        assertEquals("true", sender.getJavaMailProperties().getProperty("mail.smtp.auth"));
        assertEquals("true", sender.getJavaMailProperties().getProperty("mail.smtp.starttls.required"));
        assertEquals("5000", sender.getJavaMailProperties().getProperty("mail.smtp.connectiontimeout"));
        assertEquals("false", sender.getJavaMailProperties().getProperty("mail.debug"));
        environment.withProperty("spring.mail.host", "localhost");
        sender = (JavaMailSenderImpl) new IdentityConfig().identityMailSender(environment);
        assertEquals("localhost", sender.getHost());
    }

    @Test
    void rechazaConfiguracionDeEnlacesInseguraYCaducidadInvalida() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var properties = new IdentityProperties();
            assertTrue(validator.validate(properties).isEmpty());
            properties.setFrontendBaseUrl(URI.create("https://user:password@portal.example.test/?token=secreto"));
            assertFalse(validator.validate(properties).isEmpty());
            properties.setFrontendBaseUrl(URI.create("https://portal.example.test"));
            properties.setResetTokenTtl(Duration.ZERO);
            assertFalse(validator.validate(properties).isEmpty());
        }
    }
}
