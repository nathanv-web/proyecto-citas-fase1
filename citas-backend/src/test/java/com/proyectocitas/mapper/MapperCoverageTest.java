package com.proyectocitas.mapper;

import com.proyectocitas.dto.*;
import com.proyectocitas.model.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MapperCoverageTest {

    private final AppointmentMapper appointmentMapper = new AppointmentMapper();
    private final EstadoCitaMapper estadoCitaMapper = new EstadoCitaMapper();
    private final HorarioDisponibleMapper horarioMapper = new HorarioDisponibleMapper();
    private final MedicoMapper medicoMapper = new MedicoMapper();
    private final RolMapper rolMapper = new RolMapper();
    private final UsuarioMapper usuarioMapper = new UsuarioMapper();

    @Test
    void appointmentNull() {
        assertNull(appointmentMapper.toDTO(null));
        assertNull(appointmentMapper.toEntity(null));
    }

    @Test
    void appointmentCompleto() {
        Usuario paciente = new Usuario();
        paciente.setNombre("Ana");
        paciente.setApellido("Lopez");

        Usuario usuarioMedico = new Usuario();
        usuarioMedico.setNombre("Carlos");
        usuarioMedico.setApellido("Perez");

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre("Cardiologia");

        Medico medico = new Medico();
        medico.setUsuario(usuarioMedico);
        medico.setEspecialidad(especialidad);

        HorarioDisponible horario = new HorarioDisponible();
        horario.setFecha(LocalDate.of(2026, 10, 10));
        horario.setHoraInicio(LocalTime.of(8, 0));
        horario.setHoraFin(LocalTime.of(9, 0));
        horario.setMedico(medico);

        EstadoCita estado = new EstadoCita();
        estado.setNombre(EstadoCita.NombreEstado.PENDIENTE);

        Cita cita = new Cita();
        cita.setIdCita(1L);
        cita.setPaciente(paciente);
        cita.setHorario(horario);
        cita.setEstado(estado);
        cita.setMotivo("Consulta");
        cita.setDiagnosticoReceta("Reposo");
        cita.setObservaciones("Ninguna");

        AppointmentResponseDTO dto = appointmentMapper.toDTO(cita);

        assertEquals("Ana Lopez", dto.getPaciente());
        assertEquals("Carlos Perez", dto.getMedico());
        assertEquals("Cardiologia", dto.getEspecialidad());
        assertEquals("PENDIENTE", dto.getEstado());
        assertEquals("Consulta", dto.getMotivo());
    }

    @Test
    void appointmentSinRelaciones() {
        Cita cita = new Cita();

        AppointmentResponseDTO dto = appointmentMapper.toDTO(cita);

        assertNotNull(dto);
        assertNull(dto.getPaciente());
        assertNull(dto.getMedico());
        assertNull(dto.getEstado());
    }

    @Test
    void appointmentMedicoSinDatos() {
        Medico medico = new Medico();
        medico.setUsuario(null);
        medico.setEspecialidad(null);

        HorarioDisponible horario = new HorarioDisponible();
        horario.setMedico(medico);

        EstadoCita estado = new EstadoCita();
        estado.setNombre(null);

        Cita cita = new Cita();
        cita.setHorario(horario);
        cita.setEstado(estado);

        AppointmentResponseDTO dto = appointmentMapper.toDTO(cita);

        assertNull(dto.getMedico());
        assertNull(dto.getEspecialidad());
        assertNull(dto.getEstado());
    }

    @Test
    void appointmentRequestAEntity() {
        AppointmentRequestDTO dto = new AppointmentRequestDTO();
        dto.setMotivo("Dolor de cabeza");

        Cita cita = appointmentMapper.toEntity(dto);

        assertEquals("Dolor de cabeza", cita.getMotivo());
    }

    @Test
    void estadoNull() {
        assertNull(estadoCitaMapper.toDTO(null));
    }

    @Test
    void estadoCompleto() {
        EstadoCita estado = new EstadoCita();
        estado.setIdEstado(2L);
        estado.setNombre(EstadoCita.NombreEstado.COMPLETADA);
        estado.setDescripcion("Completada");

        EstadoCitaDTO dto = estadoCitaMapper.toDTO(estado);

        assertEquals(2L, dto.getIdEstado());
        assertEquals("COMPLETADA", dto.getNombre());
        assertEquals("Completada", dto.getDescripcion());
    }

    @Test
    void estadoSinNombre() {
        EstadoCita estado = new EstadoCita();
        estado.setNombre(null);

        assertNull(estadoCitaMapper.toDTO(estado).getNombre());
    }

    @Test
    void horarioNull() {
        assertNull(horarioMapper.toDTO(null));
        assertNull(horarioMapper.toEntity(null));
    }

    @Test
    void horarioCompleto() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Maria");
        usuario.setApellido("Gomez");

        Medico medico = new Medico();
        medico.setIdMedico(7L);
        medico.setUsuario(usuario);

        HorarioDisponible horario = new HorarioDisponible();
        horario.setIdHorario(10L);
        horario.setMedico(medico);
        horario.setFecha(LocalDate.of(2026, 11, 1));
        horario.setHoraInicio(LocalTime.of(10, 0));
        horario.setHoraFin(LocalTime.of(11, 0));
        horario.setEstado(HorarioDisponible.EstadoHorario.DISPONIBLE);

        HorarioDisponibleDTO dto = horarioMapper.toDTO(horario);

        assertEquals(10L, dto.getIdHorario());
        assertEquals(7L, dto.getIdMedico());
        assertEquals("Maria Gomez", dto.getMedico());
        assertEquals("DISPONIBLE", dto.getEstado());
    }

    @Test
    void horarioSinMedicoNiEstado() {
        HorarioDisponible horario = new HorarioDisponible();
        horario.setEstado(null);
        horario.setMedico(null);

        HorarioDisponibleDTO dto = horarioMapper.toDTO(horario);

        assertNull(dto.getEstado());
        assertNull(dto.getIdMedico());
    }

    @Test
    void horarioMedicoSinUsuario() {
        Medico medico = new Medico();
        medico.setIdMedico(8L);

        HorarioDisponible horario = new HorarioDisponible();
        horario.setMedico(medico);

        HorarioDisponibleDTO dto = horarioMapper.toDTO(horario);

        assertEquals(8L, dto.getIdMedico());
        assertNull(dto.getMedico());
    }

    @Test
    void horarioRequestAEntity() {
        HorarioDisponibleRequestDTO dto = new HorarioDisponibleRequestDTO();
        dto.setFecha(LocalDate.of(2026, 12, 1));
        dto.setHoraInicio(LocalTime.of(13, 0));
        dto.setHoraFin(LocalTime.of(14, 0));

        HorarioDisponible horario = horarioMapper.toEntity(dto);

        assertEquals(LocalDate.of(2026, 12, 1), horario.getFecha());
        assertEquals(LocalTime.of(13, 0), horario.getHoraInicio());
        assertEquals(LocalTime.of(14, 0), horario.getHoraFin());
    }

    @Test
    void medicoNull() {
        assertNull(medicoMapper.toDTO(null));
        assertNull(medicoMapper.toEntity(null));
    }

    @Test
    void medicoCompleto() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Luis");
        usuario.setApellido("Diaz");
        usuario.setCorreo("luis@hospital.com");

        Especialidad especialidad = new Especialidad();
        especialidad.setNombre("Pediatria");

        Medico medico = new Medico();
        medico.setIdMedico(5L);
        medico.setUsuario(usuario);
        medico.setEspecialidad(especialidad);
        medico.setColegiado("COL-100");
        medico.setAniosExperiencia(8);
        medico.setBiografia("Especialista");
        medico.setEstado(Medico.EstadoMedico.ACTIVO);

        MedicoDTO dto = medicoMapper.toDTO(medico);

        assertEquals("Luis Diaz", dto.getNombre());
        assertEquals("luis@hospital.com", dto.getCorreo());
        assertEquals("Pediatria", dto.getEspecialidad());
        assertEquals("ACTIVO", dto.getEstado());
    }

    @Test
    void medicoSinRelaciones() {
        Medico medico = new Medico();
        medico.setUsuario(null);
        medico.setEspecialidad(null);
        medico.setEstado(null);

        MedicoDTO dto = medicoMapper.toDTO(medico);

        assertNull(dto.getNombre());
        assertNull(dto.getEspecialidad());
        assertNull(dto.getEstado());
    }

    @Test
    void medicoRequestAEntity() {
        MedicoRequestDTO dto = new MedicoRequestDTO();
        dto.setColegiado("COL-200");
        dto.setAniosExperiencia(4);
        dto.setBiografia("Medico general");

        Medico medico = medicoMapper.toEntity(dto);

        assertEquals("COL-200", medico.getColegiado());
        assertEquals(4, medico.getAniosExperiencia());
        assertEquals("Medico general", medico.getBiografia());
    }

    @Test
    void rolRequestAEntity() {
        RolRequestDTO dto = new RolRequestDTO();
        dto.setNombre("ROLE_JEFE_AREA");
        dto.setDescripcion("Jefe de area");

        Rol rol = rolMapper.toEntity(dto);

        assertEquals("ROLE_JEFE_AREA", rol.getNombre());
        assertEquals("Jefe de area", rol.getDescripcion());
    }

    @Test
    void rolEntityADto() {
        Rol rol = new Rol();
        rol.setIdRol(3L);
        rol.setNombre("ROLE_ADMIN");
        rol.setDescripcion("Administrador");
        rol.setPermisos(
                Set.of(Permiso.CREAR_ROLES, Permiso.VER_USUARIOS)
        );

        RolDTO dto = rolMapper.toDTO(rol);

        assertEquals(3L, dto.getIdRol());
        assertEquals("ROLE_ADMIN", dto.getNombre());
        assertTrue(dto.getPermisos().contains("CREAR_ROLES"));
        assertTrue(dto.getPermisos().contains("VER_USUARIOS"));
    }

    @Test
    void usuarioNull() {
        assertNull(usuarioMapper.toDTO(null));
        assertNull(usuarioMapper.toEntity(null));
    }

    @Test
    void usuarioCompleto() {
        Rol admin = new Rol();
        admin.setNombre("ROLE_ADMIN");

        Rol doctor = new Rol();
        doctor.setNombre("ROLE_DOCTOR");

        Usuario usuario = new Usuario();
        usuario.setIdUsuario(20L);
        usuario.setNombre("Pedro");
        usuario.setApellido("Ruiz");
        usuario.setCorreo("pedro@correo.com");
        usuario.setTelefono("5555-5555");
        usuario.setActivo(true);
        usuario.setRoles(Set.of(admin, doctor));

        UsuarioDTO dto = usuarioMapper.toDTO(usuario);

        assertEquals(20L, dto.getIdUsuario());
        assertEquals("Pedro", dto.getNombre());
        assertEquals("Ruiz", dto.getApellido());
        assertTrue(dto.getActivo());
        assertEquals(
                Set.of("ROLE_ADMIN", "ROLE_DOCTOR"),
                dto.getRoles()
        );
    }

    @Test
    void usuarioSinRoles() {
        Usuario usuario = new Usuario();
        usuario.setRoles(null);

        UsuarioDTO dto = usuarioMapper.toDTO(usuario);

        assertNull(dto.getRoles());
    }

    @Test
    void usuarioRequestAEntity() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNombre("Sofia");
        dto.setApellido("Mendez");
        dto.setCorreo("sofia@correo.com");
        dto.setTelefono("4444-4444");
        dto.setContrasena("clave1234");

        Usuario usuario = usuarioMapper.toEntity(dto);

        assertEquals("Sofia", usuario.getNombre());
        assertEquals("Mendez", usuario.getApellido());
        assertEquals("sofia@correo.com", usuario.getCorreo());
        assertEquals("4444-4444", usuario.getTelefono());
        assertEquals("clave1234", usuario.getContrasena());
    }

    // Tres pruebas adicionales de variantes de enums

    @Test
    void horarioReservado() {
        HorarioDisponible horario = new HorarioDisponible();
        horario.setEstado(HorarioDisponible.EstadoHorario.RESERVADO);

        assertEquals(
                "RESERVADO",
                horarioMapper.toDTO(horario).getEstado()
        );
    }

    @Test
    void medicoInactivo() {
        Medico medico = new Medico();
        medico.setEstado(Medico.EstadoMedico.INACTIVO);

        assertEquals(
                "INACTIVO",
                medicoMapper.toDTO(medico).getEstado()
        );
    }

    @Test
    void citaCancelada() {
        EstadoCita estado = new EstadoCita();
        estado.setNombre(EstadoCita.NombreEstado.CANCELADA);

        Cita cita = new Cita();
        cita.setEstado(estado);

        assertEquals(
                "CANCELADA",
                appointmentMapper.toDTO(cita).getEstado()
        );
    }
}