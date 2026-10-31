package com.proyectocitas.service;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.Notificacion;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.NotificacionRepository;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CitaPacienteListener {
    private static final Logger logger = LoggerFactory.getLogger(CitaPacienteListener.class);
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaRepository citas;
    private final NotificacionRepository notificaciones;
    private final ApplicationEventPublisher eventos;
    private final MailService correo;

    public CitaPacienteListener(CitaRepository citas, NotificacionRepository notificaciones,
            ApplicationEventPublisher eventos, MailService correo) {
        this.citas = citas;
        this.notificaciones = notificaciones;
        this.eventos = eventos;
        this.correo = correo;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void guardarAviso(CitaPacienteEvent evento) {
        Cita cita = citas.findById(evento.idCita()).orElseThrow();
        String detalle = "cita #" + cita.getIdCita() + " del "
                + cita.getHorario().getFecha().format(FECHA) + " a las "
                + cita.getHorario().getHoraInicio().format(HORA);
        String titulo;
        String mensaje;
        switch (evento.tipo()) {
            case CITA_RESERVADA -> {
                titulo = "Cita confirmada";
                mensaje = "Tu " + detalle + " ha sido reservada correctamente.";
            }
            case CITA_REPROGRAMADA -> {
                titulo = "Cita reprogramada";
                mensaje = "Tu cita fue reprogramada. El nuevo horario corresponde a la " + detalle + ".";
            }
            case CITA_CANCELADA -> {
                titulo = "Cita cancelada";
                mensaje = "Tu " + detalle + " ha sido cancelada.";
            }
            case DIAGNOSTICO_DISPONIBLE -> {
                titulo = "Diagnóstico disponible";
                mensaje = "El diagnóstico de tu " + detalle
                        + " ya está disponible. Inicia sesión y consulta tu historial.";
            }
            case RECORDATORIO_CITA -> {
                titulo = "Recordatorio de cita";
                mensaje = "Recuerda tu " + detalle + ". Tu atención será en las próximas 24 horas.";
            }
            default -> throw new IllegalArgumentException("Tipo de aviso de cita no admitido");
        }

        Notificacion aviso = new Notificacion();
        aviso.setDestinatario(cita.getPaciente());
        aviso.setTitulo(titulo);
        aviso.setMensaje(mensaje);
        aviso.setTipo(evento.tipo());
        notificaciones.save(aviso);

        // La reserva ya envía una confirmación desde CitaReservadaListener.
        if (evento.tipo() != Notificacion.Tipo.CITA_RESERVADA) {
            eventos.publishEvent(new CorreoPacienteEvent(cita.getPaciente().getCorreo(), titulo, mensaje));
        }
    }

    @Async("notificationMailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enviarCorreo(CorreoPacienteEvent evento) {
        try {
            correo.send(evento.destinatario(), "CitaNova - " + evento.titulo(), evento.mensaje());
        } catch (Exception ex) {
            logger.warn("No se pudo enviar por correo un aviso de cita al paciente");
        }
    }

    public record CorreoPacienteEvent(String destinatario, String titulo, String mensaje) {
    }
}
