package com.proyectocitas.service;

import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;
import com.proyectocitas.model.Medico;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;

import com.proyectocitas.model.EstadoCita;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ScheduleServiceTest {

    private HorarioDisponibleRepository horarioRepository;
    private MedicoRepository medicoRepository;
    private CitaRepository citaRepository;
    private ScheduleService service;

    @BeforeEach
    void setUp() {
        horarioRepository = mock(HorarioDisponibleRepository.class);
        medicoRepository = mock(MedicoRepository.class);
        citaRepository = mock(CitaRepository.class);

        service = new ScheduleService(
                horarioRepository,
                medicoRepository,
                citaRepository
        );
    }

    @Test
    void consultarHorariosConIdNullDebeFallar() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarHorariosDisponibles(
                        null,
                        LocalDate.of(2026, 10, 1)
                )
        );
    }

    @Test
    void consultarHorariosConMedicoInexistenteDebeFallar() {
        when(medicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.consultarHorariosDisponibles(
                        99L,
                        LocalDate.of(2026, 10, 1)
                )
        );
    }

    @Test
    void noPublicaDisponibilidadDeMedicoInactivo() {
        Medico inactivo = new Medico();
        inactivo.setIdMedico(1L);
        inactivo.setEstado(Medico.EstadoMedico.INACTIVO);
        when(medicoRepository.findById(1L)).thenReturn(Optional.of(inactivo));

        assertThrows(ResourceNotFoundException.class,
                () -> service.consultarHorariosDisponibles(1L, LocalDate.now(ZoneId.of("America/Guatemala")).plusDays(1)));
        verifyNoInteractions(horarioRepository);
    }

    @Test
    void consultarHorariosConFechaNullDebeFallar() {
        medicoActivo(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarHorariosDisponibles(1L, null)
        );
    }

    @Test
    void consultarHorariosDebeRetornarDisponibles() {
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Guatemala"));
        LocalDate fecha = ahora.toLocalDate();
        HorarioDisponible pasado = new HorarioDisponible();
        pasado.setFecha(ahora.minusHours(1).toLocalDate());
        pasado.setHoraInicio(ahora.minusHours(1).toLocalTime());
        pasado.setEstado(EstadoHorario.DISPONIBLE);
        HorarioDisponible futuro = new HorarioDisponible();
        futuro.setFecha(ahora.plusHours(1).toLocalDate());
        futuro.setHoraInicio(ahora.plusHours(1).toLocalTime());
        futuro.setEstado(EstadoHorario.DISPONIBLE);

        medicoActivo(1L);

        when(horarioRepository
                .findByMedico_IdMedicoAndFechaAndEstado(
                        1L,
                        fecha,
                        EstadoHorario.DISPONIBLE
                ))
                .thenReturn(List.of(pasado, futuro));

        List<HorarioDisponible> resultado =
                service.consultarHorariosDisponibles(1L, fecha);

        assertEquals(List.of(futuro), resultado);

        when(horarioRepository.findByMedico_IdMedicoAndFechaAndEstado(
                1L, fecha.minusDays(1), EstadoHorario.DISPONIBLE))
                .thenReturn(List.of(pasado));
        assertTrue(service.consultarHorariosDisponibles(1L, fecha.minusDays(1)).isEmpty());
    }

    @Test
    void horarioInexistenteDebeFallar() {
        when(horarioRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.estaDisponible(99L)
        );
    }

    @Test
    void horarioReservadoNoDebeEstarDisponible() {
        HorarioDisponible horario = new HorarioDisponible();
        horario.setEstado(EstadoHorario.RESERVADO);

        when(horarioRepository.findById(1L))
                .thenReturn(Optional.of(horario));

        assertFalse(service.estaDisponible(1L));
    }

    @Test
    void horarioConCitaNoDebeEstarDisponible() {
        HorarioDisponible horario = new HorarioDisponible();
        horario.setEstado(EstadoHorario.DISPONIBLE);

        when(horarioRepository.findById(1L))
                .thenReturn(Optional.of(horario));

        when(citaRepository
        .existsByHorario_IdHorarioAndEstado_NombreNot(
                1L,
                EstadoCita.NombreEstado.CANCELADA
        ))
        .thenReturn(true);

        assertFalse(service.estaDisponible(1L));
    }

    @Test
    void horarioLibreDebeEstarDisponible() {
        HorarioDisponible horario = new HorarioDisponible();
        horario.setEstado(EstadoHorario.DISPONIBLE);

        when(horarioRepository.findById(1L))
                .thenReturn(Optional.of(horario));

        when(citaRepository
        .existsByHorario_IdHorarioAndEstado_NombreNot(
                1L,
                EstadoCita.NombreEstado.CANCELADA
        ))
        .thenReturn(false);

        assertTrue(service.estaDisponible(1L));
    }

    @Test
    void nuevoHorarioConFechaNullDebeFallar() {
        medicoActivo(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarNuevoHorario(
                        1L,
                        null,
                        LocalTime.of(8, 0),
                        LocalTime.of(9, 0)
                )
        );
    }

    @Test
    void nuevoHorarioConHoraInicioNullDebeFallar() {
        medicoActivo(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarNuevoHorario(
                        1L,
                        LocalDate.of(2026, 10, 1),
                        null,
                        LocalTime.of(9, 0)
                )
        );
    }

    @Test
    void nuevoHorarioConHoraFinNullDebeFallar() {
        medicoActivo(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarNuevoHorario(
                        1L,
                        LocalDate.of(2026, 10, 1),
                        LocalTime.of(8, 0),
                        null
                )
        );
    }

    @Test
    void horaFinIgualAHoraInicioDebeFallar() {
        medicoActivo(1L);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.validarNuevoHorario(
                        1L,
                        LocalDate.of(2026, 10, 1),
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 0)
                )
        );
    }

    @Test
    void horarioTraslapadoDebeRetornarFalse() {
        LocalDate fecha = LocalDate.of(2026, 10, 1);

        HorarioDisponible existente = new HorarioDisponible();
        existente.setHoraInicio(LocalTime.of(8, 0));
        existente.setHoraFin(LocalTime.of(10, 0));

        medicoActivo(1L);

        when(horarioRepository
                .findByMedico_IdMedicoAndFecha(1L, fecha))
                .thenReturn(List.of(existente));

        assertFalse(
                service.validarNuevoHorario(
                        1L,
                        fecha,
                        LocalTime.of(9, 0),
                        LocalTime.of(11, 0)
                )
        );
    }

    @Test
    void horarioSinTraslapeDebeRetornarTrue() {
        LocalDate fecha = LocalDate.of(2026, 10, 1);

        HorarioDisponible existente = new HorarioDisponible();
        existente.setHoraInicio(LocalTime.of(8, 0));
        existente.setHoraFin(LocalTime.of(9, 0));

        medicoActivo(1L);

        when(horarioRepository
                .findByMedico_IdMedicoAndFecha(1L, fecha))
                .thenReturn(List.of(existente));

        assertTrue(
                service.validarNuevoHorario(
                        1L,
                        fecha,
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void hayTraslapeDebeRetornarTrue() {
        assertTrue(
                service.hayTraslape(
                        LocalTime.of(9, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(10, 0),
                        LocalTime.of(12, 0)
                )
        );
    }

    @Test
    void horariosConsecutivosNoDebenTraslaparse() {
        assertFalse(
                service.hayTraslape(
                        LocalTime.of(10, 0),
                        LocalTime.of(11, 0),
                        LocalTime.of(8, 0),
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void horarioAnteriorNoDebeTraslaparse() {
        assertFalse(
                service.hayTraslape(
                        LocalTime.of(8, 0),
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 0),
                        LocalTime.of(10, 0)
                )
        );
    }

    @Test
    void crearHorarioBloqueaMedicoAntesDeValidarYGuardar() {
        Medico medico = new Medico();
        medico.setIdMedico(1L);
        LocalDate fecha = LocalDate.now(ZoneId.of("America/Guatemala")).plusDays(1);
        when(medicoRepository.buscarMedicoConBloqueo(1L)).thenReturn(Optional.of(medico));
        when(horarioRepository.findByMedico_IdMedicoAndFecha(1L, fecha)).thenReturn(List.of());
        when(horarioRepository.save(any(HorarioDisponible.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        HorarioDisponible creado = service.crearHorarioAtomico(
                1L, fecha, LocalTime.of(9, 0), LocalTime.of(10, 0));

        assertSame(medico, creado.getMedico());
        org.mockito.InOrder orden = inOrder(medicoRepository, horarioRepository);
        orden.verify(medicoRepository).buscarMedicoConBloqueo(1L);
        orden.verify(horarioRepository).findByMedico_IdMedicoAndFecha(1L, fecha);
        orden.verify(horarioRepository).save(creado);
    }

    @Test
    void actualizarHorarioBloqueaElHorarioAntesDeComprobarCitas() {
        Medico medico = new Medico();
        medico.setIdMedico(1L);
        HorarioDisponible horario = new HorarioDisponible();
        horario.setIdHorario(10L);
        horario.setMedico(medico);
        horario.setFecha(LocalDate.now(ZoneId.of("America/Guatemala")).plusDays(2));
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(10, 0));
        LocalDate nuevaFecha = LocalDate.now(ZoneId.of("America/Guatemala")).plusDays(3);
        when(medicoRepository.buscarMedicoConBloqueo(1L)).thenReturn(Optional.of(medico));
        when(horarioRepository.buscarHorarioConBloqueo(10L)).thenReturn(Optional.of(horario));
        when(citaRepository.existsByHorario_IdHorario(10L)).thenReturn(false);
        when(horarioRepository.findByMedico_IdMedicoAndFecha(1L, nuevaFecha)).thenReturn(List.of());
        when(horarioRepository.save(horario)).thenReturn(horario);

        service.actualizarHorarioAtomico(
                10L, 1L, nuevaFecha, LocalTime.of(11, 0), LocalTime.of(12, 0));

        org.mockito.InOrder orden = inOrder(medicoRepository, horarioRepository, citaRepository);
        orden.verify(medicoRepository).buscarMedicoConBloqueo(1L);
        orden.verify(horarioRepository).buscarHorarioConBloqueo(10L);
        orden.verify(citaRepository).existsByHorario_IdHorario(10L);
        orden.verify(horarioRepository).findByMedico_IdMedicoAndFecha(1L, nuevaFecha);
        orden.verify(horarioRepository).save(horario);
    }

    private void medicoActivo(Long idMedico) {
        Medico medico = new Medico();
        medico.setIdMedico(idMedico);
        when(medicoRepository.findById(idMedico)).thenReturn(Optional.of(medico));
    }
}
