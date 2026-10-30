package com.proyectocitas.service;

import com.proyectocitas.model.Notificacion;
import com.proyectocitas.repository.NotificacionRepository;
import com.proyectocitas.repository.UsuarioRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CitaReservadaListener {

    private static final Logger logger =
            LoggerFactory.getLogger(CitaReservadaListener.class);

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final MailService mailService;

    public CitaReservadaListener(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository,
            MailService mailService) {

        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.mailService = mailService;
    }

    // ==============================================
    // GUARDAR AVISO AL MEDICO
    // ANTES DE CONFIRMAR LA TRANSACCION
    // ==============================================

    @TransactionalEventListener(
            phase = TransactionPhase.BEFORE_COMMIT
    )
    public void guardarNotificacion(CitaReservadaEvent evento) {

        Notificacion notificacion = new Notificacion();

        notificacion.setDestinatario(
                usuarioRepository.getReferenceById(
                        evento.idUsuarioMedico()
                )
        );

        notificacion.setTitulo("Nueva cita reservada");

        notificacion.setMensaje(
                "Tienes una nueva cita para el "
                + evento.fecha()
                + " a las "
                + evento.horaInicio()
        );

        notificacion.setTipo(
                Notificacion.Tipo.CITA_RESERVADA
        );

        notificacion.setLeida(false);

        notificacionRepository.save(notificacion);
    }

    // ==============================================
    // ENVIAR CORREO DESPUES DEL COMMIT
    // ==============================================

    @Async("identityMailExecutor")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void enviarConfirmacion(CitaReservadaEvent evento) {

        try {

            String asunto = "CitaNova - Reserva de cita registrada";

            String mensaje =
                    "Tu cita ha sido registrada correctamente.\n\n"
                    + "Numero de cita: " + evento.idCita()
                    + "\nFecha: " + evento.fecha()
                    + "\nHora: " + evento.horaInicio()
                    + "\n\nPuedes consultar los detalles en CitaNova."
                    + "\n\nGracias por utilizar nuestro sistema.";

            mailService.send(
                    evento.correoPaciente(),
                    asunto,
                    mensaje
            );

        } catch (Exception ex) {

            // No mostrar direcciones de correo ni datos sensibles
            logger.warn(
                    "No se pudo enviar una confirmacion de cita"
            );
        }
    }
}