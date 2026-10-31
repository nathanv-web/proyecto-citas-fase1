package com.proyectocitas.service;

import com.proyectocitas.dto.request.RescheduleRequestDTO;
import com.proyectocitas.dto.response.AppointmentResponseDTO;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;
import com.proyectocitas.mapper.AppointmentMapper;
import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Medico;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.UsuarioRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReprogramacionCitaServiceTest {

    private CitaRepository citaRepository;
    private HorarioDisponibleRepository horarioRepository;
    private UsuarioRepository usuarioRepository;
    private AppointmentService service;
    private Usuario paciente;
    private HorarioDisponible horarioAnterior;
    private HorarioDisponible horarioNuevo;
    private Cita cita;
    private RescheduleRequestDTO request;

    @BeforeEach
    void prepararDatos() {
        citaRepository = mock(CitaRepository.class);
        horarioRepository = mock(HorarioDisponibleRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        service = new AppointmentService(citaRepository, usuarioRepository,
                mock(EstadoCitaRepository.class), horarioRepository, new AppointmentMapper(),
                mock(ApplicationEventPublisher.class));

        paciente = new Usuario();
        paciente.setIdUsuario(1L);
        paciente.setCorreo("paciente@correo.com");
        paciente.setNombre("Ana");
        paciente.setApellido("Lopez");

        Medico medico = new Medico();
        medico.setIdMedico(5L);
        horarioAnterior = new HorarioDisponible();
        horarioAnterior.setIdHorario(10L);
        horarioAnterior.setMedico(medico);
        horarioAnterior.setEstado(HorarioDisponible.EstadoHorario.RESERVADO);
        horarioNuevo = new HorarioDisponible();
        horarioNuevo.setIdHorario(20L);
        horarioNuevo.setMedico(medico);
        horarioNuevo.setFecha(LocalDate.now().plusDays(7));
        horarioNuevo.setHoraInicio(LocalTime.of(10, 0));
        horarioNuevo.setHoraFin(LocalTime.of(10, 30));

        EstadoCita pendiente = new EstadoCita();
        pendiente.setNombre(EstadoCita.NombreEstado.PENDIENTE);
        cita = new Cita();
        cita.setIdCita(3L);
        cita.setPaciente(paciente);
        cita.setHorario(horarioAnterior);
        cita.setEstado(pendiente);
        cita.setMotivo("Consulta general");

        request = new RescheduleRequestDTO();
        request.setIdHorarioNuevo(20L);
        when(usuarioRepository.findByCorreo(paciente.getCorreo())).thenReturn(Optional.of(paciente));
        when(citaRepository.obtenerIdHorarioDeCita(3L)).thenReturn(Optional.of(10L));
        when(horarioRepository.buscarHorarioConBloqueo(10L)).thenReturn(Optional.of(horarioAnterior));
        when(horarioRepository.buscarHorarioConBloqueo(20L)).thenReturn(Optional.of(horarioNuevo));
        when(citaRepository.findById(3L)).thenReturn(Optional.of(cita));
    }

    // =================================================
    // REPROGRAMAR Y CONSERVAR LOS DATOS DE LA CITA
    // =================================================

    @Test
    void debeReprogramarCitaLiberandoElHorarioAnteriorYReservandoElNuevo() {
        when(citaRepository.save(cita)).thenReturn(cita);

        AppointmentResponseDTO resultado = reprogramar();

        assertSame(horarioNuevo, cita.getHorario());
        assertEquals(HorarioDisponible.EstadoHorario.DISPONIBLE, horarioAnterior.getEstado());
        assertEquals(HorarioDisponible.EstadoHorario.RESERVADO, horarioNuevo.getEstado());
        assertEquals(3L, resultado.getIdCita());
        assertEquals(5L, resultado.getIdMedico());
        assertEquals("Ana Lopez", resultado.getPaciente());
        assertEquals("Consulta general", resultado.getMotivo());
        assertEquals("PENDIENTE", resultado.getEstado());
        assertEquals(horarioNuevo.getFecha(), resultado.getFecha());
        assertEquals(LocalTime.of(10, 0), resultado.getHoraInicio());
        assertEquals(LocalTime.of(10, 30), resultado.getHoraFin());

        var orden = inOrder(horarioRepository, citaRepository);
        orden.verify(horarioRepository).buscarHorarioConBloqueo(10L);
        orden.verify(horarioRepository).buscarHorarioConBloqueo(20L);
        orden.verify(citaRepository).findById(3L);
        orden.verify(horarioRepository).save(horarioAnterior);
        orden.verify(horarioRepository).save(horarioNuevo);
        orden.verify(citaRepository).save(cita);
    }

    @Test
    void debeBloquearEnOrdenAscendenteAunqueElNuevoHorarioTengaUnIdMenor() {
        horarioAnterior.setIdHorario(20L);
        horarioNuevo.setIdHorario(10L);
        request.setIdHorarioNuevo(10L);
        when(citaRepository.obtenerIdHorarioDeCita(3L)).thenReturn(Optional.of(20L));
        when(horarioRepository.buscarHorarioConBloqueo(10L)).thenReturn(Optional.of(horarioNuevo));
        when(horarioRepository.buscarHorarioConBloqueo(20L)).thenReturn(Optional.of(horarioAnterior));
        when(citaRepository.save(cita)).thenReturn(cita);

        assertEquals("PENDIENTE", reprogramar().getEstado());
        assertSame(horarioNuevo, cita.getHorario());
        assertEquals(HorarioDisponible.EstadoHorario.DISPONIBLE, horarioAnterior.getEstado());
        assertEquals(HorarioDisponible.EstadoHorario.RESERVADO, horarioNuevo.getEstado());
        var orden = inOrder(horarioRepository);
        orden.verify(horarioRepository).buscarHorarioConBloqueo(10L);
        orden.verify(horarioRepository).buscarHorarioConBloqueo(20L);
    }

    // =================================================
    // VALIDAR PACIENTE, CITA Y HORARIOS
    // =================================================

    @Test
    void reprogramarDebeRetornar401SiPacienteNoExiste() {
        when(usuarioRepository.findByCorreo(paciente.getCorreo())).thenReturn(Optional.empty());

        comprobarError(HttpStatus.UNAUTHORIZED);
        verifyNoInteractions(horarioRepository);
    }

    @Test
    void reprogramarDebeFallarSiCitaNoExiste() {
        when(citaRepository.obtenerIdHorarioDeCita(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
        verifyNoInteractions(horarioRepository);
    }

    @Test
    void reprogramarDebeRechazarElMismoHorario() {
        request.setIdHorarioNuevo(10L);

        comprobarError(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(horarioRepository);
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 20L})
    void reprogramarDebeFallarSiUnoDeLosHorariosNoExiste(Long idHorario) {
        when(horarioRepository.buscarHorarioConBloqueo(idHorario)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    @Test
    void reprogramarDebeFallarSiCitaDesapareceAlAdquirirLosBloqueos() {
        when(citaRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    @Test
    void reprogramarDebeRetornar403SiCitaPerteneceAOtroPaciente() {
        Usuario otroPaciente = new Usuario();
        otroPaciente.setIdUsuario(99L);
        cita.setPaciente(otroPaciente);

        comprobarError(HttpStatus.FORBIDDEN);
    }

    @Test
    void reprogramarDebeRetornar403SiCitaNoTienePaciente() {
        cita.setPaciente(null);

        comprobarError(HttpStatus.FORBIDDEN);
    }

    @Test
    void reprogramarDebeFallarSiElHorarioDeLaCitaCambio() {
        HorarioDisponible otroHorario = new HorarioDisponible();
        otroHorario.setIdHorario(99L);
        cita.setHorario(otroHorario);

        assertThrows(ScheduleConflictException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    @Test
    void reprogramarDebeFallarSiCitaYaNoTieneHorario() {
        cita.setHorario(null);

        assertThrows(ScheduleConflictException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoCita.NombreEstado.class, names = {"CANCELADA", "COMPLETADA"})
    void reprogramarDebeFallarSiCitaNoEstaPendiente(EstadoCita.NombreEstado estado) {
        cita.getEstado().setNombre(estado);

        assertThrows(ResourceConflictException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    @Test
    void reprogramarDebeFallarSiCitaNoTieneEstado() {
        cita.setEstado(null);

        assertThrows(ResourceConflictException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    // =================================================
    // CONSERVAR MEDICO Y VALIDAR DISPONIBILIDAD
    // =================================================

    @Test
    void reprogramarDebeRechazarUnHorarioDeOtroMedico() {
        Medico otroMedico = new Medico();
        otroMedico.setIdMedico(99L);
        horarioNuevo.setMedico(otroMedico);

        comprobarError(HttpStatus.BAD_REQUEST);
    }

    @ParameterizedTest
    @ValueSource(longs = {10L, 20L})
    void reprogramarDebeRechazarUnHorarioSinMedico(Long idHorario) {
        (idHorario == 10L ? horarioAnterior : horarioNuevo).setMedico(null);

        comprobarError(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reprogramarDebeFallarSiHorarioAnteriorYaNoEstaReservado() {
        horarioAnterior.setEstado(HorarioDisponible.EstadoHorario.DISPONIBLE);

        assertThrows(ScheduleConflictException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    @ParameterizedTest
    @EnumSource(value = HorarioDisponible.EstadoHorario.class, names = {"RESERVADO", "NO_DISPONIBLE"})
    void reprogramarDebeFallarSiHorarioNuevoNoEstaDisponible(HorarioDisponible.EstadoHorario estado) {
        horarioNuevo.setEstado(estado);

        assertThrows(ScheduleConflictException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
    }

    @Test
    void reprogramarDebeRechazarUnHorarioPasado() {
        horarioNuevo.setFecha(LocalDate.now().minusDays(1));

        comprobarError(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reprogramarDebeRechazarUnHorarioSinFecha() {
        horarioNuevo.setFecha(null);

        comprobarError(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reprogramarDebeRechazarUnHorarioSinHoraDeInicio() {
        horarioNuevo.setHoraInicio(null);

        comprobarError(HttpStatus.BAD_REQUEST);
    }

    @Test
    void reprogramarDebeFallarSiHorarioNuevoYaTieneUnaCitaVigente() {
        when(citaRepository.existsByHorario_IdHorarioAndEstado_NombreNot(
                20L, EstadoCita.NombreEstado.CANCELADA)).thenReturn(true);

        assertThrows(ScheduleConflictException.class, this::reprogramar);
        comprobarQueNoSeGuardaronCambios();
        assertSame(horarioAnterior, cita.getHorario());
        assertEquals(HorarioDisponible.EstadoHorario.RESERVADO, horarioAnterior.getEstado());
        assertEquals(HorarioDisponible.EstadoHorario.DISPONIBLE, horarioNuevo.getEstado());
    }

    private AppointmentResponseDTO reprogramar() {
        return service.reprogramarCita(3L, request, paciente.getCorreo());
    }

    private void comprobarError(HttpStatus estado) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class, this::reprogramar);
        assertEquals(estado, error.getStatusCode());
        comprobarQueNoSeGuardaronCambios();
    }

    private void comprobarQueNoSeGuardaronCambios() {
        verify(citaRepository, never()).save(any(Cita.class));
        verify(horarioRepository, never()).save(any(HorarioDisponible.class));
    }
}
