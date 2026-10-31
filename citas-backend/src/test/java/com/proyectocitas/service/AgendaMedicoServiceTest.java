package com.proyectocitas.service;

import com.proyectocitas.dto.response.DoctorAgendaResponseDTO;
import com.proyectocitas.mapper.AppointmentMapper;
import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AgendaMedicoServiceTest {

    private static final String CORREO_MEDICO = "doctor@clinica.com";
    private static final LocalDate FECHA = LocalDate.of(2026, 10, 28);
    private CitaRepository citaRepository;
    private UsuarioRepository usuarioRepository;
    private AppointmentService service;
    private Cita cita;

    @BeforeEach
    void prepararDatos() {
        citaRepository = mock(CitaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new AppointmentService(citaRepository, usuarioRepository,
                mock(EstadoCitaRepository.class), mock(HorarioDisponibleRepository.class),
                new AppointmentMapper(), mock(ApplicationEventPublisher.class));
        when(usuarioRepository.findByCorreo(CORREO_MEDICO)).thenReturn(Optional.of(new Usuario()));

        Usuario paciente = new Usuario();
        paciente.setNombre("Ana");
        paciente.setApellido("Lopez");
        HorarioDisponible horario = new HorarioDisponible();
        horario.setFecha(FECHA);
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(9, 30));
        EstadoCita estado = new EstadoCita();
        estado.setNombre(EstadoCita.NombreEstado.PENDIENTE);
        cita = new Cita();
        cita.setIdCita(1L);
        cita.setPaciente(paciente);
        cita.setHorario(horario);
        cita.setEstado(estado);
        cita.setMotivo("Consulta general");
    }

    // =================================================
    // CONSULTAR AGENDA POR DIA, SEMANA Y MES
    // =================================================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"dia", " DIA ", "   "})
    void debeConsultarElDiaSolicitadoCuandoElPeriodoEsDiarioOEstaVacio(String periodo) {
        when(citaRepository.findAgendaByCorreoMedicoAndFecha(CORREO_MEDICO, FECHA))
                .thenReturn(List.of(cita));

        List<DoctorAgendaResponseDTO> resultado = service.obtenerMiAgenda(
                CORREO_MEDICO, FECHA, periodo, null, null);

        comprobarDatosDeAgenda(resultado);
        verify(citaRepository, never()).findAgendaByCorreoMedicoAndRango(anyString(), any(), any());
    }

    @ParameterizedTest
    @CsvSource({
        "2026-10-28, semana, 2026-10-26, 2026-11-01",
        "2027-01-01, ' SEMANA ', 2026-12-28, 2027-01-03",
        "2026-10-28, mes, 2026-10-01, 2026-10-31",
        "2028-02-15, ' MES ', 2028-02-01, 2028-02-29"
    })
    void debeCalcularElRangoSemanalOMensualIncluyendoCambiosDeAnioYFebreroBisiesto(
            LocalDate referencia, String periodo, LocalDate inicio, LocalDate fin) {
        when(citaRepository.findAgendaByCorreoMedicoAndRango(CORREO_MEDICO, inicio, fin))
                .thenReturn(List.of(cita));

        comprobarDatosDeAgenda(service.obtenerMiAgenda(CORREO_MEDICO, referencia, periodo, null, null));

        verify(citaRepository, never()).findAgendaByCorreoMedicoAndFecha(anyString(), any());
    }

    @Test
    void debeConsultarUnRangoPersonalizado() {
        LocalDate fin = LocalDate.of(2026, 11, 4);
        when(citaRepository.findAgendaByCorreoMedicoAndRango(CORREO_MEDICO, FECHA, fin))
                .thenReturn(List.of(cita));

        comprobarDatosDeAgenda(service.obtenerMiAgenda(CORREO_MEDICO, null, null, FECHA, fin));
    }

    @Test
    void debeConsultarUnSoloDiaCuandoElRangoTieneLaMismaFecha() {
        when(citaRepository.findAgendaByCorreoMedicoAndFecha(CORREO_MEDICO, FECHA))
                .thenReturn(List.of(cita));

        comprobarDatosDeAgenda(service.obtenerMiAgenda(CORREO_MEDICO, null, null, FECHA, FECHA));
    }

    @Test
    void debeConsultarElDiaActualCuandoNoSeEnvianFiltros() {
        LocalDate antes = LocalDate.now();
        when(citaRepository.findAgendaByCorreoMedicoAndFecha(eq(CORREO_MEDICO), any(LocalDate.class)))
                .thenReturn(List.of(cita));

        comprobarDatosDeAgenda(service.obtenerMiAgenda(CORREO_MEDICO));

        LocalDate despues = LocalDate.now();
        verify(citaRepository).findAgendaByCorreoMedicoAndFecha(eq(CORREO_MEDICO),
                argThat(fecha -> !fecha.isBefore(antes) && !fecha.isAfter(despues)));
    }

    @Test
    void debeRetornarAgendaVaciaCuandoNoHayCitas() {
        when(citaRepository.findAgendaByCorreoMedicoAndFecha(CORREO_MEDICO, FECHA))
                .thenReturn(List.of());

        assertTrue(service.obtenerMiAgenda(CORREO_MEDICO, FECHA, "dia", null, null).isEmpty());
        verify(citaRepository).findAgendaByCorreoMedicoAndFecha(CORREO_MEDICO, FECHA);
    }

    // =================================================
    // RECHAZAR USUARIOS Y FILTROS INVALIDOS
    // =================================================

    @Test
    void agendaDebeRetornar401SiUsuarioNoExiste() {
        when(usuarioRepository.findByCorreo(CORREO_MEDICO)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.obtenerMiAgenda(CORREO_MEDICO));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(citaRepository);
    }

    @ParameterizedTest
    @CsvSource({
        ",,2026-10-28,",
        ",,,2026-10-29",
        "2026-10-28,,2026-10-28,2026-10-29",
        ",semana,2026-10-28,2026-10-29"
    })
    void agendaDebeRechazarRangosIncompletosOMezcladosConOtrosFiltros(
            LocalDate fecha, String periodo, LocalDate desde, LocalDate hasta) {
        comprobarFiltroInvalido(fecha, periodo, desde, hasta);
    }

    @Test
    void agendaDebeRechazarUnPeriodoDesconocido() {
        comprobarFiltroInvalido(FECHA, "anio", null, null);
    }

    @Test
    void agendaDebeRechazarUnRangoConFechaInicialPosteriorALaFinal() {
        comprobarFiltroInvalido(null, null, FECHA.plusDays(1), FECHA);
    }

    // =================================================
    // CONSULTAR TODAS LAS CITAS
    // =================================================

    @Test
    void debeListarTodasLasCitasConLosDatosDelPaciente() {
        when(citaRepository.findAll()).thenReturn(List.of(cita));

        var resultado = service.obtenerTodasLasCitas();

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getIdCita());
        assertEquals("Ana Lopez", resultado.get(0).getPaciente());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
        assertEquals(FECHA, resultado.get(0).getFecha());
    }

    private void comprobarFiltroInvalido(LocalDate fecha, String periodo, LocalDate desde, LocalDate hasta) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.obtenerMiAgenda(CORREO_MEDICO, fecha, periodo, desde, hasta));
        assertEquals(HttpStatus.BAD_REQUEST, error.getStatusCode());
        verifyNoInteractions(citaRepository);
    }

    private void comprobarDatosDeAgenda(List<DoctorAgendaResponseDTO> resultado) {
        assertEquals(1, resultado.size());
        DoctorAgendaResponseDTO turno = resultado.get(0);
        assertEquals(1L, turno.getIdCita());
        assertEquals("Ana Lopez", turno.getPaciente());
        assertEquals(FECHA, turno.getFecha());
        assertEquals(LocalTime.of(9, 0), turno.getHoraInicio());
        assertEquals(LocalTime.of(9, 30), turno.getHoraFin());
        assertEquals("Consulta general", turno.getMotivo());
        assertEquals("PENDIENTE", turno.getEstado());
    }
}
