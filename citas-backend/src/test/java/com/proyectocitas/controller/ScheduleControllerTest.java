package com.proyectocitas.controller;

import com.proyectocitas.dto.HorarioDisponibleDTO;
import com.proyectocitas.dto.HorarioDisponibleRequestDTO;
import com.proyectocitas.dto.HorarioUpdateRequestDTO;
import com.proyectocitas.exception.ScheduleConflictException;
import com.proyectocitas.mapper.HorarioDisponibleMapper;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Medico;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.service.ScheduleService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleControllerTest {

    @Mock
    private ScheduleService scheduleService;

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private HorarioDisponibleRepository horarioRepository;

    @Mock
    private HorarioDisponibleMapper horarioMapper;

    @Mock
    private CitaRepository citaRepository;

    @InjectMocks
    private ScheduleController controller;


    @Test
    void debeCrearHorarioCorrectamente() {

        Principal principal = () -> "doctor@clinica.com";

        Medico medico = new Medico();
        medico.setIdMedico(1L);

        HorarioDisponibleRequestDTO request =
                new HorarioDisponibleRequestDTO();

        request.setFecha(LocalDate.now().plusDays(1));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(9, 0));

        HorarioDisponible horario = new HorarioDisponible();
        HorarioDisponibleDTO dto = new HorarioDisponibleDTO();

        when(medicoRepository.findByUsuario_Correo("doctor@clinica.com"))
                .thenReturn(Optional.of(medico));

        when(scheduleService.validarNuevoHorario(
                1L,
                request.getFecha(),
                request.getHoraInicio(),
                request.getHoraFin()))
                .thenReturn(true);

        when(horarioMapper.toEntity(request))
                .thenReturn(horario);

        when(horarioRepository.save(horario))
                .thenReturn(horario);

        when(horarioMapper.toDTO(horario))
                .thenReturn(dto);

        var response =
                controller.crearHorario(request, principal);

        assertEquals(
                HttpStatus.CREATED,
                response.getStatusCode()
        );
    }


    @Test
    void noDebeCrearHorarioEnFechaPasada() {

        Principal principal = () -> "doctor@clinica.com";

        Medico medico = new Medico();
        medico.setIdMedico(1L);

        HorarioDisponibleRequestDTO request =
                new HorarioDisponibleRequestDTO();

        request.setFecha(LocalDate.now().minusDays(1));
        request.setHoraInicio(LocalTime.of(8, 0));
        request.setHoraFin(LocalTime.of(9, 0));

        when(medicoRepository.findByUsuario_Correo("doctor@clinica.com"))
                .thenReturn(Optional.of(medico));

        assertThrows(
                ScheduleConflictException.class,
                () -> controller.crearHorario(request, principal)
        );

        verify(horarioRepository, never())
                .save(any());
    }


    @Test
    void noDebeActualizarHorarioHaciaFechaPasada() {

        Principal principal = () -> "doctor@clinica.com";

        Medico medico = new Medico();
        medico.setIdMedico(1L);

        HorarioDisponible horario = new HorarioDisponible();
        horario.setIdHorario(10L);
        horario.setMedico(medico);
        horario.setFecha(LocalDate.now().plusDays(1));
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(9, 0));

        HorarioUpdateRequestDTO request =
                new HorarioUpdateRequestDTO();

        request.setFecha(LocalDate.now().minusDays(1));
        request.setHoraInicio(LocalTime.of(10, 0));
        request.setHoraFin(LocalTime.of(11, 0));

        when(medicoRepository.findByUsuario_Correo("doctor@clinica.com"))
                .thenReturn(Optional.of(medico));

        when(horarioRepository.findById(10L))
                .thenReturn(Optional.of(horario));

        assertThrows(
                ScheduleConflictException.class,
                () -> controller.actualizarHorario(
                        10L,
                        request,
                        principal
                )
        );

        verify(horarioRepository, never())
                .save(any());
    }
}