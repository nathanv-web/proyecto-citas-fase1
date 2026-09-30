package com.proyectocitas.service;

import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
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
        when(medicoRepository.existsById(99L)).thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> service.consultarHorariosDisponibles(
                        99L,
                        LocalDate.of(2026, 10, 1)
                )
        );
    }

    @Test
    void consultarHorariosConFechaNullDebeFallar() {
        when(medicoRepository.existsById(1L)).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consultarHorariosDisponibles(1L, null)
        );
    }

    @Test
    void consultarHorariosDebeRetornarDisponibles() {
        LocalDate fecha = LocalDate.of(2026, 10, 1);

        HorarioDisponible horario = new HorarioDisponible();

        when(medicoRepository.existsById(1L)).thenReturn(true);

        when(horarioRepository
                .findByMedico_IdMedicoAndFechaAndEstado(
                        1L,
                        fecha,
                        EstadoHorario.DISPONIBLE
                ))
                .thenReturn(List.of(horario));

        List<HorarioDisponible> resultado =
                service.consultarHorariosDisponibles(1L, fecha);

        assertEquals(1, resultado.size());
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

        when(citaRepository.existsByHorario_IdHorario(1L))
                .thenReturn(true);

        assertFalse(service.estaDisponible(1L));
    }

    @Test
    void horarioLibreDebeEstarDisponible() {
        HorarioDisponible horario = new HorarioDisponible();
        horario.setEstado(EstadoHorario.DISPONIBLE);

        when(horarioRepository.findById(1L))
                .thenReturn(Optional.of(horario));

        when(citaRepository.existsByHorario_IdHorario(1L))
                .thenReturn(false);

        assertTrue(service.estaDisponible(1L));
    }

    @Test
    void nuevoHorarioConFechaNullDebeFallar() {
        when(medicoRepository.existsById(1L)).thenReturn(true);

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
        when(medicoRepository.existsById(1L)).thenReturn(true);

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
        when(medicoRepository.existsById(1L)).thenReturn(true);

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
        when(medicoRepository.existsById(1L)).thenReturn(true);

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

        when(medicoRepository.existsById(1L)).thenReturn(true);

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

        when(medicoRepository.existsById(1L)).thenReturn(true);

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
}