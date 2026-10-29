package com.proyectocitas.service;

import com.proyectocitas.config.IdentityProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/** Única infraestructura de correo, reutilizable por el integrante de citas/notificaciones. */
@Service
public class MailService {
    private final JavaMailSender sender;
    private final IdentityProperties properties;

    public MailService(JavaMailSender sender, IdentityProperties properties) {
        this.sender = sender;
        this.properties = properties;
    }

    public void sendPasswordRecovery(PasswordRecoveryService.TokenDelivery delivery) {
        String link = UriComponentsBuilder.fromUri(properties.getFrontendBaseUrl())
                .path("/reset-password").queryParam("token", delivery.token()).build().toUriString();
        send(delivery.email(), "CitaNova: restablecer contraseña",
                "Solicitaste restablecer tu contraseña. Abre este enlace:\n" + link
                + "\n\nEl enlace es temporal y solo puede utilizarse una vez."
                + "\nSi no realizaste esta solicitud, ignora este correo. No compartas el enlace.");
    }

    public void send(String recipient, String subject, String body) {
        var message = new SimpleMailMessage();
        message.setFrom(properties.getMailFrom());
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        sender.send(message);
    }

    public void sendEmailVerification(EmailVerificationService.TokenDelivery delivery) {
        String link = UriComponentsBuilder.fromUri(properties.getFrontendBaseUrl())
                .path("/verify-email").queryParam("token", delivery.token()).build().toUriString();
        send(delivery.email(), "CitaNova: verifica tu correo",
                "Confirma tu dirección de correo abriendo este enlace:\n" + link
                + "\n\nEl enlace es temporal y solo puede utilizarse una vez."
                + "\nSi no realizaste esta solicitud, ignora este correo. No compartas el enlace.");
    }
}
