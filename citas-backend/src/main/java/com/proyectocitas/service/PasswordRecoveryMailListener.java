package com.proyectocitas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class PasswordRecoveryMailListener {
    private static final Logger logger = LoggerFactory.getLogger(PasswordRecoveryMailListener.class);
    private final PasswordRecoveryService recovery;
    private final MailService mail;

    public PasswordRecoveryMailListener(PasswordRecoveryService recovery, MailService mail) {
        this.recovery = recovery;
        this.mail = mail;
    }

    @Async("identityMailExecutor")
    @EventListener
    public void onRequest(PasswordRecoveryRequested event) {
        try {
            // issueToken retorna después del commit; no enviar enlaces de transacciones fallidas.
            recovery.issueToken(event.email()).ifPresent(mail::sendPasswordRecovery);
        } catch (Exception ex) {
            // No registrar el objeto de excepción: SMTP puede incluir direcciones o el contenido.
            logger.warn("No fue posible completar un envío de recuperación");
        }
    }
}
