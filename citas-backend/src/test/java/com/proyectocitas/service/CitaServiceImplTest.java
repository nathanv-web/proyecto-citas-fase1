package com.proyectocitas.service;

import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;
import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.CitaServiceImpl;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CitaServiceImplTest {

    @Test
    void debeLanzarScheduleConflictCuandoHorarioYaFueReservado() {

        // Repositorios simulados
        CitaRepository citaRepository =
                mock(CitaRepository.class);

        HorarioDisponibleRepository horarioRepository =
                mock(HorarioDisponibleRepository.class);

        UsuarioRepository usuarioRepository =
                mock(UsuarioRepository.class);

        EstadoCitaRepository estadoCitaRepository =
                mock(EstadoCitaRepository.class);


        // Servicio que vamos a probar
        CitaServiceImpl service =
                new CitaServiceImpl(
                        citaRepository,
                        horarioRepository,
                        usuarioRepository,
                        estadoCitaRepository
                );


        // Horario que intenta seleccionar el paciente
        HorarioDisponible horarioEntrada =
                new HorarioDisponible();

        horarioEntrada.setIdHorario(10L);


        // Cita recibida
        Cita cita =
                new Cita();

        cita.setHorario(horarioEntrada);


        // Horario existente en la base de datos
        HorarioDisponible horarioDB =
                new HorarioDisponible();

        horarioDB.setIdHorario(10L);
        horarioDB.setEstado(
                EstadoHorario.DISPONIBLE
        );


        // Simular que encontramos el horario
        when(
                horarioRepository.findById(10L)
        )
                .thenReturn(
                        Optional.of(horarioDB)
                );


        // Simular que ese horario YA tiene una cita
        when(
                citaRepository
                        .existsByHorario_IdHorario(10L)
        )
                .thenReturn(true);


        // Esperamos nuestra excepción específica
        assertThrows(ScheduleConflictException.class,
                () -> service.guardar(cita)
        );
        
        
    }
    
    @Test
void debeLanzarScheduleConflictCuandoHorarioNoEstaDisponible() {

    CitaRepository citaRepository =
            mock(CitaRepository.class);

    HorarioDisponibleRepository horarioRepository =
            mock(HorarioDisponibleRepository.class);

    UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    EstadoCitaRepository estadoCitaRepository =
            mock(EstadoCitaRepository.class);


    CitaServiceImpl service =
            new CitaServiceImpl(
                    citaRepository,
                    horarioRepository,
                    usuarioRepository,
                    estadoCitaRepository
            );


    // Horario enviado en la cita
    HorarioDisponible horarioEntrada =
            new HorarioDisponible();

    horarioEntrada.setIdHorario(10L);


    Cita cita =
            new Cita();

    cita.setHorario(horarioEntrada);


    // Horario encontrado en BD,
    // pero ya se encuentra RESERVADO
    HorarioDisponible horarioDB =
            new HorarioDisponible();

    horarioDB.setIdHorario(10L);
    horarioDB.setEstado(
            EstadoHorario.RESERVADO
    );


    when(
            horarioRepository.findById(10L)
    )
            .thenReturn(
                    Optional.of(horarioDB)
            );


    // No existe otra cita asociada,
    // pero el estado ya no es DISPONIBLE
    when(
            citaRepository
                    .existsByHorario_IdHorario(10L)
    )
            .thenReturn(false);


    assertThrows(ScheduleConflictException.class,
            () -> service.guardar(cita)
    );
}
@Test
void debeLanzarResourceNotFoundCuandoHorarioNoExiste() {

    CitaRepository citaRepository =
            mock(CitaRepository.class);

    HorarioDisponibleRepository horarioRepository =
            mock(HorarioDisponibleRepository.class);

    UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    EstadoCitaRepository estadoCitaRepository =
            mock(EstadoCitaRepository.class);


    CitaServiceImpl service =
            new CitaServiceImpl(
                    citaRepository,
                    horarioRepository,
                    usuarioRepository,
                    estadoCitaRepository
            );


    HorarioDisponible horarioEntrada =
            new HorarioDisponible();

    horarioEntrada.setIdHorario(99L);


    Cita cita =
            new Cita();

    cita.setHorario(horarioEntrada);


    // Simulamos que el horario 99 NO existe
    when(
            horarioRepository.findById(99L)
    )
            .thenReturn(
                    Optional.empty()
            );


    assertThrows(
            ResourceNotFoundException.class,
            () -> service.guardar(cita)
    );
}

@Test
void debeLanzarResourceNotFoundCuandoPacienteNoExiste() {

    CitaRepository citaRepository =
            mock(CitaRepository.class);

    HorarioDisponibleRepository horarioRepository =
            mock(HorarioDisponibleRepository.class);

    UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    EstadoCitaRepository estadoCitaRepository =
            mock(EstadoCitaRepository.class);


    CitaServiceImpl service =
            new CitaServiceImpl(
                    citaRepository,
                    horarioRepository,
                    usuarioRepository,
                    estadoCitaRepository
            );


    // Horario enviado
    HorarioDisponible horarioEntrada =
            new HorarioDisponible();

    horarioEntrada.setIdHorario(10L);


    // Paciente enviado
    Usuario pacienteEntrada =
            new Usuario();

    pacienteEntrada.setIdUsuario(99L);


    Cita cita =
            new Cita();

    cita.setHorario(horarioEntrada);
    cita.setPaciente(pacienteEntrada);


    // El horario sí existe y está disponible
    HorarioDisponible horarioDB =
            new HorarioDisponible();

    horarioDB.setIdHorario(10L);
    horarioDB.setEstado(
            EstadoHorario.DISPONIBLE
    );


    when(
            horarioRepository.findById(10L)
    )
            .thenReturn(
                    Optional.of(horarioDB)
            );


    // No hay otra cita utilizando el horario
    when(
            citaRepository
                    .existsByHorario_IdHorario(10L)
    )
            .thenReturn(false);


    // El paciente 99 NO existe
    when(
            usuarioRepository.findById(99L)
    )
            .thenReturn(
                    Optional.empty()
            );


    assertThrows(
            ResourceNotFoundException.class,
            () -> service.guardar(cita)
    );
}
@Test
void debeLanzarResourceNotFoundCuandoEstadoCitaNoExiste() {

    CitaRepository citaRepository =
            mock(CitaRepository.class);

    HorarioDisponibleRepository horarioRepository =
            mock(HorarioDisponibleRepository.class);

    UsuarioRepository usuarioRepository =
            mock(UsuarioRepository.class);

    EstadoCitaRepository estadoCitaRepository =
            mock(EstadoCitaRepository.class);


    CitaServiceImpl service =
            new CitaServiceImpl(
                    citaRepository,
                    horarioRepository,
                    usuarioRepository,
                    estadoCitaRepository
            );


    // Horario enviado
    HorarioDisponible horarioEntrada =
            new HorarioDisponible();

    horarioEntrada.setIdHorario(10L);


    // Paciente enviado
    Usuario pacienteEntrada =
            new Usuario();

    pacienteEntrada.setIdUsuario(5L);


    // Estado enviado
    EstadoCita estadoEntrada =
            new EstadoCita();

    estadoEntrada.setIdEstado(99L);


    Cita cita = new Cita();

    cita.setHorario(horarioEntrada);
    cita.setPaciente(pacienteEntrada);
    cita.setEstado(estadoEntrada);


    // Horario existente y disponible
    HorarioDisponible horarioDB =
            new HorarioDisponible();

    horarioDB.setIdHorario(10L);
    horarioDB.setEstado(
            EstadoHorario.DISPONIBLE
    );


    Usuario pacienteDB =
            new Usuario();

    pacienteDB.setIdUsuario(5L);


    when(
            horarioRepository.findById(10L)
    )
            .thenReturn(
                    Optional.of(horarioDB)
            );


    when(
            citaRepository
                    .existsByHorario_IdHorario(10L)
    )
            .thenReturn(false);


    when(
            usuarioRepository.findById(5L)
    )
            .thenReturn(
                    Optional.of(pacienteDB)
            );


    // El estado 99 NO existe
    when(
            estadoCitaRepository.findById(99L)
    )
            .thenReturn(
                    Optional.empty()
            );


    assertThrows(
            ResourceNotFoundException.class,
            () -> service.guardar(cita)
    );
}
    
    
}