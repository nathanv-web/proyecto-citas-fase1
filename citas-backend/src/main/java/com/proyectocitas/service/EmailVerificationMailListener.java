package com.proyectocitas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class EmailVerificationMailListener {
    private static final Logger logger = LoggerFactory.getLogger(EmailVerificationMailListener.class);
    private final EmailVerificationService verification;
    private final MailService mail;

    public EmailVerificationMailListener(EmailVerificationService verification, MailService mail) {
        this.verification = verification;
        this.mail = mail;
    }

    @Async("identityMailExecutor")
    @EventListener
    public void onRequest(EmailVerificationRequested event) {
        try {
            verification.issueToken(event.email()).ifPresent(mail::sendEmailVerification);
        } catch (Exception ex) {
            logger.warn("No fue posible completar un envío de verificación de correo");
        }
    }
}
