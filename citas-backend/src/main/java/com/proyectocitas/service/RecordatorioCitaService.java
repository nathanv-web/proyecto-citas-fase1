package com.proyectocitas.service;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Notificacion;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecordatorioCitaService {
    private final CitaRepository citas;
    private final HorarioDisponibleRepository horarios;
    private final ApplicationEventPublisher eventos;
    private final Clock reloj;

    public RecordatorioCitaService(CitaRepository citas, HorarioDisponibleRepository horarios,
            ApplicationEventPublisher eventos, @Qualifier("identityClock") Clock reloj,
            @Value("${app.notifications.time-zone:America/Guatemala}") String zonaHoraria) {
        this.citas = citas;
        this.horarios = horarios;
        this.eventos = eventos;
        this.reloj = reloj.withZone(ZoneId.of(zonaHoraria));
    }

    @Transactional(readOnly = true)
    public List<Long> buscarPendientes() {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime limite = ahora.plusHours(24);
        return citas.buscarCitasParaRecordatorio(EstadoCita.NombreEstado.PENDIENTE,
                ahora.toLocalDate(), ahora.toLocalTime(), limite.toLocalDate(), limite.toLocalTime(),
                PageRequest.of(0, 200));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void enviarRecordatorio(Long idCita) {
        Long idHorario = citas.obtenerIdHorarioDeCita(idCita).orElse(null);
        if (idHorario == null) return;

        // Se usa el mismo bloqueo que cancelar, reprogramar y diagnosticar.
        // Otra instancia del servidor debe volver a comprobar la marca después de esperar.
        HorarioDisponible horario = horarios.buscarHorarioConBloqueo(idHorario).orElse(null);
        if (horario == null) return;
        Cita cita = citas.findById(idCita).orElse(null);
        if (cita == null || cita.isRecordatorioEnviado()
                || cita.getHorario() == null
                || !Objects.equals(cita.getHorario().getIdHorario(), idHorario)
                || cita.getEstado().getNombre() != EstadoCita.NombreEstado.PENDIENTE
                || horario.getEstado() != HorarioDisponible.EstadoHorario.RESERVADO) return;

        LocalDateTime ahora = LocalDateTime.now(reloj);
        LocalDateTime atencion = LocalDateTime.of(horario.getFecha(), horario.getHoraInicio());
        if (!atencion.isAfter(ahora) || atencion.isAfter(ahora.plusHours(24))) return;

        cita.setRecordatorioEnviado(true);
        citas.save(cita);
        eventos.publishEvent(new CitaPacienteEvent(idCita, Notificacion.Tipo.RECORDATORIO_CITA));
    }
}
