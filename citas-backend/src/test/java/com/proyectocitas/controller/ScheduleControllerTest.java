package com.proyectocitas.controller;

import com.proyectocitas.dto.HorarioDisponibleRequestDTO;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;
import com.proyectocitas.mapper.HorarioDisponibleMapper;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.service.ScheduleService;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScheduleControllerTest {

    @Test
    void debeLanzarScheduleConflictCuandoHorarioSeCruza() {

        ScheduleService scheduleService =
                mock(ScheduleService.class);

        MedicoRepository medicoRepository =
                mock(MedicoRepository.class);

        HorarioDisponibleRepository horarioRepository =
                mock(HorarioDisponibleRepository.class);

        HorarioDisponibleMapper horarioMapper =
                mock(HorarioDisponibleMapper.class);


        ScheduleController controller =
                new ScheduleController(
                        scheduleService,
                        medicoRepository,
                        horarioRepository,
                        horarioMapper
                );


        HorarioDisponibleRequestDTO request =
                new HorarioDisponibleRequestDTO();

        request.setIdMedico(1L);
        request.setFecha(
                LocalDate.of(2026, 9, 29)
        );

        request.setHoraInicio(
                LocalTime.of(8, 0)
        );

        request.setHoraFin(
                LocalTime.of(9, 0)
        );


        when(
                scheduleService.validarNuevoHorario(
                        1L,
                        request.getFecha(),
                        request.getHoraInicio(),
                        request.getHoraFin()
                )
        )
                .thenReturn(false);


        assertThrows(
                ScheduleConflictException.class,
                () -> controller.crearHorario(request)
        );
    }
    @Test
void debeLanzarResourceNotFoundCuandoMedicoNoExiste() {

    ScheduleService scheduleService =
            mock(ScheduleService.class);

    MedicoRepository medicoRepository =
            mock(MedicoRepository.class);

    HorarioDisponibleRepository horarioRepository =
            mock(HorarioDisponibleRepository.class);

    HorarioDisponibleMapper horarioMapper =
            mock(HorarioDisponibleMapper.class);


    ScheduleController controller =
            new ScheduleController(
                    scheduleService,
                    medicoRepository,
                    horarioRepository,
                    horarioMapper
            );


    HorarioDisponibleRequestDTO request =
            new HorarioDisponibleRequestDTO();

    request.setIdMedico(99L);
    request.setFecha(
            LocalDate.of(2026, 9, 29)
    );

    request.setHoraInicio(
            LocalTime.of(8, 0)
    );

    request.setHoraFin(
            LocalTime.of(9, 0)
    );


    // El horario no tiene conflictos
    when(
            scheduleService.validarNuevoHorario(
                    99L,
                    request.getFecha(),
                    request.getHoraInicio(),
                    request.getHoraFin()
            )
    )
            .thenReturn(true);


    // Pero el médico 99 no existe
    when(
            medicoRepository.findById(99L)
    )
            .thenReturn(
                    Optional.empty()
            );


    assertThrows(
            ResourceNotFoundException.class,
            () -> controller.crearHorario(request)
    );
}
}