package com.proyectocitas.service;

import com.proyectocitas.dto.AppointmentRequestDTO;
import com.proyectocitas.dto.AppointmentResponseDTO;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.exception.ScheduleConflictException;
import com.proyectocitas.mapper.AppointmentMapper;
import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.dto.DiagnosisRequestDTO;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.model.Medico;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    private CitaRepository citaRepository;
    private UsuarioRepository usuarioRepository;
    private EstadoCitaRepository estadoCitaRepository;
    private HorarioDisponibleRepository horarioRepository;
    private AppointmentMapper appointmentMapper;

    private AppointmentService service;


    @BeforeEach
    void setUp() {

        citaRepository = mock(CitaRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        estadoCitaRepository = mock(EstadoCitaRepository.class);
        horarioRepository = mock(HorarioDisponibleRepository.class);
        appointmentMapper = mock(AppointmentMapper.class);

        service = new AppointmentService(
                citaRepository,
                usuarioRepository,
                estadoCitaRepository,
                horarioRepository,
                appointmentMapper
        );
    }


    // =================================================
    // AGENDAR CITA CORRECTAMENTE
    // =================================================

    @Test
    void debeAgendarCitaCorrectamente() {

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setIdHorario(10L);
        request.setMotivo("  Dolor de cabeza  ");


        Usuario paciente = new Usuario();
        paciente.setIdUsuario(1L);


        HorarioDisponible horario =
                new HorarioDisponible();

        horario.setIdHorario(10L);
        horario.setEstado(
                HorarioDisponible.EstadoHorario.DISPONIBLE
        );


        EstadoCita pendiente =
                new EstadoCita();

        pendiente.setNombre(
                EstadoCita.NombreEstado.PENDIENTE
        );


        Cita cita = new Cita();
        Cita citaGuardada = new Cita();

        AppointmentResponseDTO respuesta =
                new AppointmentResponseDTO();


        when(
                usuarioRepository.findByCorreo(
                        "paciente@correo.com"
                )
        ).thenReturn(
                Optional.of(paciente)
        );


        when(
                horarioRepository.findById(10L)
        ).thenReturn(
                Optional.of(horario)
        );


        when(
                citaRepository
                        .existsByHorario_IdHorario(10L)
        ).thenReturn(false);


        when(
                estadoCitaRepository.findByNombre(
                        EstadoCita.NombreEstado.PENDIENTE
                )
        ).thenReturn(
                Optional.of(pendiente)
        );


        when(
                appointmentMapper.toEntity(request)
        ).thenReturn(cita);


        when(
                citaRepository.save(cita)
        ).thenReturn(citaGuardada);


        when(
                appointmentMapper.toDTO(citaGuardada)
        ).thenReturn(respuesta);


        AppointmentResponseDTO resultado =
                service.agendarCita(
                        request,
                        "paciente@correo.com"
                );


        assertSame(respuesta, resultado);

        assertSame(
                paciente,
                cita.getPaciente()
        );

        assertSame(
                horario,
                cita.getHorario()
        );

        assertSame(
                pendiente,
                cita.getEstado()
        );

        // Comprueba que se eliminan espacios
        assertEquals(
                "Dolor de cabeza",
                cita.getMotivo()
        );

        // El horario debe quedar reservado
        assertEquals(
                HorarioDisponible.EstadoHorario.RESERVADO,
                horario.getEstado()
        );


        verify(horarioRepository)
                .save(horario);

        verify(citaRepository)
                .save(cita);
    }


    // =================================================
    // PACIENTE NO EXISTE
    // =================================================

    @Test
    void agendarDebeRetornar401SiPacienteNoExiste() {

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setIdHorario(10L);
        request.setMotivo("Consulta general");


        when(
                usuarioRepository.findByCorreo(
                        "noexiste@correo.com"
                )
        ).thenReturn(
                Optional.empty()
        );


        ResponseStatusException exception =
                assertThrows(
                        ResponseStatusException.class,
                        () -> service.agendarCita(
                                request,
                                "noexiste@correo.com"
                        )
                );


        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }


    // =================================================
    // HORARIO NO EXISTE
    // =================================================

    @Test
    void agendarDebeFallarSiHorarioNoExiste() {

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setIdHorario(99L);
        request.setMotivo("Consulta general");


        Usuario paciente = new Usuario();
        paciente.setIdUsuario(1L);


        when(
                usuarioRepository.findByCorreo(
                        "paciente@correo.com"
                )
        ).thenReturn(
                Optional.of(paciente)
        );


        when(
                horarioRepository.findById(99L)
        ).thenReturn(
                Optional.empty()
        );


        assertThrows(
                ResourceNotFoundException.class,
                () -> service.agendarCita(
                        request,
                        "paciente@correo.com"
                )
        );
    }


    // =================================================
    // HORARIO NO DISPONIBLE
    // =================================================

    @Test
    void agendarDebeFallarSiHorarioNoEstaDisponible() {

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setIdHorario(10L);
        request.setMotivo("Consulta general");


        Usuario paciente = new Usuario();
        paciente.setIdUsuario(1L);


        HorarioDisponible horario =
                new HorarioDisponible();

        horario.setIdHorario(10L);

        horario.setEstado(
                HorarioDisponible.EstadoHorario.RESERVADO
        );


        when(
                usuarioRepository.findByCorreo(
                        "paciente@correo.com"
                )
        ).thenReturn(
                Optional.of(paciente)
        );


        when(
                horarioRepository.findById(10L)
        ).thenReturn(
                Optional.of(horario)
        );


        assertThrows(
                ResourceNotFoundException.class,
                () -> service.agendarCita(
                        request,
                        "paciente@correo.com"
                )
        );
    }


    // =================================================
    // HORARIO YA TIENE UNA CITA
    // =================================================

    @Test
    void agendarDebeFallarSiHorarioYaTieneCita() {

        AppointmentRequestDTO request =
                new AppointmentRequestDTO();

        request.setIdHorario(10L);
        request.setMotivo("Consulta general");


        Usuario paciente = new Usuario();
        paciente.setIdUsuario(1L);


        HorarioDisponible horario =
                new HorarioDisponible();

        horario.setIdHorario(10L);

        horario.setEstado(
                HorarioDisponible.EstadoHorario.DISPONIBLE
        );


        when(
                usuarioRepository.findByCorreo(
                        "paciente@correo.com"
                )
        ).thenReturn(
                Optional.of(paciente)
        );


        when(
                horarioRepository.findById(10L)
        ).thenReturn(
                Optional.of(horario)
        );


        when(
                citaRepository
                        .existsByHorario_IdHorario(10L)
        ).thenReturn(true);


        assertThrows(
                ScheduleConflictException.class,
                () -> service.agendarCita(
                        request,
                        "paciente@correo.com"
                )
        );
    }
    // =================================================
// REGISTRAR DIAGNÓSTICO CORRECTAMENTE
// =================================================

@Test
void debeRegistrarDiagnosticoCorrectamente() {

    Usuario medicoUsuario = new Usuario();
    medicoUsuario.setIdUsuario(2L);

    Medico medico = new Medico();
    medico.setUsuario(medicoUsuario);

    HorarioDisponible horario = new HorarioDisponible();
    horario.setMedico(medico);

    EstadoCita pendiente = new EstadoCita();
    pendiente.setNombre(EstadoCita.NombreEstado.PENDIENTE);

    EstadoCita completada = new EstadoCita();
    completada.setNombre(EstadoCita.NombreEstado.COMPLETADA);

    Cita cita = new Cita();
    cita.setHorario(horario);
    cita.setEstado(pendiente);

    DiagnosisRequestDTO request = new DiagnosisRequestDTO();
    request.setDiagnosticoReceta("  Gripe - reposo por 3 días  ");
    request.setObservaciones("Tomar líquidos");

    AppointmentResponseDTO respuesta =
            new AppointmentResponseDTO();

    when(usuarioRepository.findByCorreo("doctor@clinica.com"))
            .thenReturn(Optional.of(medicoUsuario));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));

    when(estadoCitaRepository.findByNombre(
            EstadoCita.NombreEstado.COMPLETADA))
            .thenReturn(Optional.of(completada));

    when(citaRepository.save(cita))
            .thenReturn(cita);

    when(appointmentMapper.toDTO(cita))
            .thenReturn(respuesta);


    AppointmentResponseDTO resultado =
            service.registrarDiagnostico(
                    1L,
                    request,
                    "doctor@clinica.com"
            );


    assertSame(respuesta, resultado);

    assertEquals(
            "Gripe - reposo por 3 días",
            cita.getDiagnosticoReceta()
    );

    assertEquals(
            "Tomar líquidos",
            cita.getObservaciones()
    );

    assertSame(
            completada,
            cita.getEstado()
    );

    verify(citaRepository).save(cita);
}


// =================================================
// MÉDICO AUTENTICADO NO EXISTE
// =================================================

@Test
void diagnosticoDebeRetornar401SiUsuarioNoExiste() {

    DiagnosisRequestDTO request =
            new DiagnosisRequestDTO();

    request.setDiagnosticoReceta("Diagnóstico");

    when(usuarioRepository.findByCorreo("noexiste@correo.com"))
            .thenReturn(Optional.empty());


    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.registrarDiagnostico(
                            1L,
                            request,
                            "noexiste@correo.com"
                    )
            );


    assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
    );
}


// =================================================
// CITA NO EXISTE
// =================================================

@Test
void diagnosticoDebeFallarSiCitaNoExiste() {

    Usuario medico = new Usuario();
    medico.setIdUsuario(2L);

    DiagnosisRequestDTO request =
            new DiagnosisRequestDTO();

    request.setDiagnosticoReceta("Diagnóstico");


    when(usuarioRepository.findByCorreo("doctor@clinica.com"))
            .thenReturn(Optional.of(medico));

    when(citaRepository.findById(99L))
            .thenReturn(Optional.empty());


    assertThrows(
            ResourceNotFoundException.class,
            () -> service.registrarDiagnostico(
                    99L,
                    request,
                    "doctor@clinica.com"
            )
    );
}


// =================================================
// CITA SIN MÉDICO ASIGNADO
// =================================================

@Test
void diagnosticoDebeFallarSiCitaNoTieneMedicoAsignado() {

    Usuario medico = new Usuario();
    medico.setIdUsuario(2L);

    Cita cita = new Cita();

    // Sin horario = no hay médico correctamente asignado
    cita.setHorario(null);

    DiagnosisRequestDTO request =
            new DiagnosisRequestDTO();

    request.setDiagnosticoReceta("Diagnóstico");


    when(usuarioRepository.findByCorreo("doctor@clinica.com"))
            .thenReturn(Optional.of(medico));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));


    assertThrows(
            ResourceConflictException.class,
            () -> service.registrarDiagnostico(
                    1L,
                    request,
                    "doctor@clinica.com"
            )
    );
}

// =================================================
// MÉDICO INTENTA MODIFICAR CITA DE OTRO MÉDICO
// =================================================

@Test
void diagnosticoDebeRetornar403SiMedicoNoEsElAsignado() {

    Usuario autenticado = new Usuario();
    autenticado.setIdUsuario(2L);

    Usuario asignado = new Usuario();
    asignado.setIdUsuario(50L);

    Medico medicoAsignado = new Medico();
    medicoAsignado.setUsuario(asignado);

    HorarioDisponible horario = new HorarioDisponible();
    horario.setMedico(medicoAsignado);

    Cita cita = new Cita();
    cita.setHorario(horario);

    DiagnosisRequestDTO request =
            new DiagnosisRequestDTO();

    request.setDiagnosticoReceta("Diagnóstico");


    when(usuarioRepository.findByCorreo("doctor@clinica.com"))
            .thenReturn(Optional.of(autenticado));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));


    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.registrarDiagnostico(
                            1L,
                            request,
                            "doctor@clinica.com"
                    )
            );


    assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
    );
}


// =================================================
// CITA NO ESTÁ PENDIENTE
// =================================================

@Test
void diagnosticoDebeFallarSiCitaNoEstaPendiente() {

    Usuario usuario = new Usuario();
    usuario.setIdUsuario(2L);

    Medico medico = new Medico();
    medico.setUsuario(usuario);

    HorarioDisponible horario = new HorarioDisponible();
    horario.setMedico(medico);

    EstadoCita completada = new EstadoCita();
    completada.setNombre(
            EstadoCita.NombreEstado.COMPLETADA
    );

    Cita cita = new Cita();
    cita.setHorario(horario);
    cita.setEstado(completada);

    DiagnosisRequestDTO request =
            new DiagnosisRequestDTO();

    request.setDiagnosticoReceta("Diagnóstico");


    when(usuarioRepository.findByCorreo("doctor@clinica.com"))
            .thenReturn(Optional.of(usuario));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));


    assertThrows(
            ResourceConflictException.class,
            () -> service.registrarDiagnostico(
                    1L,
                    request,
                    "doctor@clinica.com"
            )
    );
}


// =================================================
// OBTENER HISTORIAL CORRECTAMENTE
// =================================================

@Test
void debeObtenerHistorialDelPaciente() {

    Usuario paciente = new Usuario();
    paciente.setIdUsuario(1L);

    Cita cita1 = new Cita();
    Cita cita2 = new Cita();

    AppointmentResponseDTO dto1 = new AppointmentResponseDTO();
    AppointmentResponseDTO dto2 = new AppointmentResponseDTO();

    when(usuarioRepository.findByCorreo("paciente@correo.com"))
            .thenReturn(Optional.of(paciente));

    when(citaRepository.findByPaciente_IdUsuario(1L))
            .thenReturn(List.of(cita1, cita2));

    when(appointmentMapper.toDTO(cita1)).thenReturn(dto1);
    when(appointmentMapper.toDTO(cita2)).thenReturn(dto2);


    List<AppointmentResponseDTO> resultado =
            service.obtenerMiHistorial("paciente@correo.com");


    assertEquals(2, resultado.size());
    assertSame(dto1, resultado.get(0));
    assertSame(dto2, resultado.get(1));

    verify(citaRepository)
            .findByPaciente_IdUsuario(1L);
}


// =================================================
// HISTORIAL - USUARIO NO EXISTE
// =================================================

@Test
void historialDebeRetornar401SiPacienteNoExiste() {

    when(usuarioRepository.findByCorreo("noexiste@correo.com"))
            .thenReturn(Optional.empty());


    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.obtenerMiHistorial(
                            "noexiste@correo.com"
                    )
            );


    assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
    );
}


// =================================================
// CANCELAR CITA CORRECTAMENTE
// =================================================

@Test
void debeCancelarCitaCorrectamente() {

    Usuario paciente = new Usuario();
    paciente.setIdUsuario(1L);

    EstadoCita pendiente = new EstadoCita();
    pendiente.setNombre(EstadoCita.NombreEstado.PENDIENTE);

    EstadoCita cancelada = new EstadoCita();
    cancelada.setNombre(EstadoCita.NombreEstado.CANCELADA);

    HorarioDisponible horario = new HorarioDisponible();
    horario.setEstado(
            HorarioDisponible.EstadoHorario.RESERVADO
    );

    Cita cita = new Cita();
    cita.setPaciente(paciente);
    cita.setEstado(pendiente);
    cita.setHorario(horario);

    AppointmentResponseDTO respuesta =
            new AppointmentResponseDTO();


    when(usuarioRepository.findByCorreo("paciente@correo.com"))
            .thenReturn(Optional.of(paciente));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));

    when(estadoCitaRepository.findByNombre(
            EstadoCita.NombreEstado.CANCELADA))
            .thenReturn(Optional.of(cancelada));

    when(citaRepository.save(cita))
            .thenReturn(cita);

    when(appointmentMapper.toDTO(cita))
            .thenReturn(respuesta);


    AppointmentResponseDTO resultado =
            service.cancelarCita(
                    1L,
                    "paciente@correo.com"
            );


    assertSame(respuesta, resultado);

    assertSame(
            cancelada,
            cita.getEstado()
    );

    assertEquals(
            HorarioDisponible.EstadoHorario.DISPONIBLE,
            horario.getEstado()
    );

    verify(horarioRepository).save(horario);
    verify(citaRepository).save(cita);
}


// =================================================
// CANCELAR - PACIENTE NO EXISTE
// =================================================

@Test
void cancelarDebeRetornar401SiPacienteNoExiste() {

    when(usuarioRepository.findByCorreo("noexiste@correo.com"))
            .thenReturn(Optional.empty());


    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.cancelarCita(
                            1L,
                            "noexiste@correo.com"
                    )
            );


    assertEquals(
            HttpStatus.UNAUTHORIZED,
            exception.getStatusCode()
    );
}


// =================================================
// CANCELAR - CITA NO EXISTE
// =================================================

@Test
void cancelarDebeFallarSiCitaNoExiste() {

    Usuario paciente = new Usuario();
    paciente.setIdUsuario(1L);

    when(usuarioRepository.findByCorreo("paciente@correo.com"))
            .thenReturn(Optional.of(paciente));

    when(citaRepository.findById(99L))
            .thenReturn(Optional.empty());


    assertThrows(
            ResourceNotFoundException.class,
            () -> service.cancelarCita(
                    99L,
                    "paciente@correo.com"
            )
    );
}


// =================================================
// CANCELAR CITA DE OTRO PACIENTE
// =================================================

@Test
void cancelarDebeRetornar403SiCitaEsDeOtroPaciente() {

    Usuario autenticado = new Usuario();
    autenticado.setIdUsuario(1L);

    Usuario propietario = new Usuario();
    propietario.setIdUsuario(50L);

    Cita cita = new Cita();
    cita.setPaciente(propietario);


    when(usuarioRepository.findByCorreo("paciente@correo.com"))
            .thenReturn(Optional.of(autenticado));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));


    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.cancelarCita(
                            1L,
                            "paciente@correo.com"
                    )
            );


    assertEquals(
            HttpStatus.FORBIDDEN,
            exception.getStatusCode()
    );
}


// =================================================
// CANCELAR CITA QUE NO ESTÁ PENDIENTE
// =================================================

@Test
void cancelarDebeFallarSiCitaNoEstaPendiente() {

    Usuario paciente = new Usuario();
    paciente.setIdUsuario(1L);

    EstadoCita completada = new EstadoCita();
    completada.setNombre(
            EstadoCita.NombreEstado.COMPLETADA
    );

    Cita cita = new Cita();
    cita.setPaciente(paciente);
    cita.setEstado(completada);


    when(usuarioRepository.findByCorreo("paciente@correo.com"))
            .thenReturn(Optional.of(paciente));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));


    assertThrows(
            ResourceConflictException.class,
            () -> service.cancelarCita(
                    1L,
                    "paciente@correo.com"
            )
    );
}


// =================================================
// ESTADO CANCELADA NO EXISTE
// =================================================

@Test
void cancelarDebeFallarSiEstadoCanceladaNoExiste() {

    Usuario paciente = new Usuario();
    paciente.setIdUsuario(1L);

    EstadoCita pendiente = new EstadoCita();
    pendiente.setNombre(
            EstadoCita.NombreEstado.PENDIENTE
    );

    Cita cita = new Cita();
    cita.setPaciente(paciente);
    cita.setEstado(pendiente);


    when(usuarioRepository.findByCorreo("paciente@correo.com"))
            .thenReturn(Optional.of(paciente));

    when(citaRepository.findById(1L))
            .thenReturn(Optional.of(cita));

    when(estadoCitaRepository.findByNombre(
            EstadoCita.NombreEstado.CANCELADA))
            .thenReturn(Optional.empty());


    ResponseStatusException exception =
            assertThrows(
                    ResponseStatusException.class,
                    () -> service.cancelarCita(
                            1L,
                            "paciente@correo.com"
                    )
            );


    assertEquals(
            HttpStatus.INTERNAL_SERVER_ERROR,
            exception.getStatusCode()
    );
}
}