package com.proyectocitas.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@ConditionalOnProperty(name = "app.notifications.reminders.enabled", havingValue = "true", matchIfMissing = true)
public class RecordatorioCitaScheduler {
    private static final Logger logger = LoggerFactory.getLogger(RecordatorioCitaScheduler.class);
    private final RecordatorioCitaService recordatorios;

    public RecordatorioCitaScheduler(RecordatorioCitaService recordatorios) {
        this.recordatorios = recordatorios;
    }

    @Scheduled(fixedDelayString = "${app.notifications.reminders.interval-ms:60000}",
            initialDelayString = "${app.notifications.reminders.initial-delay-ms:30000}")
    public void recordarCitasProximas() {
        for (Long idCita : recordatorios.buscarPendientes()) {
            try {
                recordatorios.enviarRecordatorio(idCita);
            } catch (Exception ex) {
                logger.warn("No se pudo preparar un recordatorio de cita");
            }
        }
    }
}
