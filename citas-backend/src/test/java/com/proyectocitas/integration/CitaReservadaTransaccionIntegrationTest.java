
package com.proyectocitas.integration;

import com.proyectocitas.model.Notificacion;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.model.Medico;
import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.Rol;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.dto.request.AppointmentCreateRequestDTO;
import com.proyectocitas.dto.response.AppointmentResponseDTO;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.EstadoCitaRepository;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import com.proyectocitas.repository.NotificacionRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.CitaReservadaEvent;
import com.proyectocitas.service.MailService;
import com.proyectocitas.service.ScheduleService;
import com.proyectocitas.service.AppointmentService;
import com.proyectocitas.service.UsuarioService;
import com.proyectocitas.exception.ScheduleConflictException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:citanova-notificaciones-tx;MODE=MySQL;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false",
    "spring.sql.init.mode=never",
    "jwt.secret=clave-exclusiva-para-pruebas-de-citanova-01234567890123456789",
    "jwt.expiration=3600000"
})
class CitaReservadaTransaccionIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publicadorEventos;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private HorarioDisponibleRepository horarioDisponibleRepository;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private EstadoCitaRepository estadoCitaRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioService usuarioService;

    @MockitoBean
    private MailService mailService;

    // =========================================
    // CREAR USUARIO MEDICO DE PRUEBA
    // =========================================

    private Usuario crearUsuarioMedico() {

        Usuario usuario = new Usuario();

        usuario.setNombre("Medico");
        usuario.setApellido("Prueba");
        usuario.setCorreo(
                "medico-" + UUID.randomUUID()
                + "@example.com"
        );
        usuario.setTelefono("55555555");
        usuario.setContraseña("clave-solo-para-pruebas");

        return usuarioRepository.saveAndFlush(usuario);
    }

    // =========================================
    // GENERAR EVENTO DE RESERVA
    // =========================================

    private CitaReservadaEvent crearEvento(
            Usuario medico,
            String correoPaciente) {

        return new CitaReservadaEvent(
                100L,
                medico.getIdUsuario(),
                correoPaciente,
                LocalDate.of(2026, 11, 10),
                LocalTime.of(9, 0)
        );
    }

    // =========================================
    // PRUEBA 1: TRANSACCION CONFIRMADA
    // =========================================

    @Test
    void debeGuardarAvisoYEnviarCorreoDespuesDelCommit() {

        Usuario medico = crearUsuarioMedico();

        String correoPaciente =
                "paciente-" + UUID.randomUUID()
                + "@example.com";

        CitaReservadaEvent evento =
                crearEvento(medico, correoPaciente);

        TransactionTemplate transaccion =
                new TransactionTemplate(transactionManager);

        // Ejecutar una transaccion real.
        transaccion.executeWithoutResult(status -> {

            publicadorEventos.publishEvent(evento);

            // El correo no debe enviarse
            // antes de confirmar la transaccion.
            verify(mailService, never()).send(
                    anyString(),
                    anyString(),
                    anyString()
            );
        });

        // La transaccion ya fue confirmada.

        List<Notificacion> avisos =
                notificacionRepository
                    .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                            medico.getIdUsuario()
                    );

        assertEquals(1, avisos.size());

        Notificacion aviso = avisos.get(0);

        assertEquals(
                "Nueva cita reservada",
                aviso.getTitulo()
        );

        assertEquals(
                Notificacion.Tipo.CITA_RESERVADA,
                aviso.getTipo()
        );

        assertFalse(aviso.isLeida());

        assertEquals(
                medico.getIdUsuario(),
                aviso.getDestinatario().getIdUsuario()
        );

        // El envio es asincrono.
        // Esperamos hasta 5 segundos.
        verify(mailService, timeout(5000)).send(
                eq(correoPaciente),
                contains("CitaNova"),
                contains("100")
        );
    }

    // =========================================
    // PRUEBA 2: TRANSACCION REVERTIDA
    // =========================================

    @Test
    void noDebeGuardarAvisoNiEnviarCorreoSiHayRollback() {

        Usuario medico = crearUsuarioMedico();

        String correoPaciente =
                "paciente-" + UUID.randomUUID()
                + "@example.com";

        CitaReservadaEvent evento =
                crearEvento(medico, correoPaciente);

        TransactionTemplate transaccion =
                new TransactionTemplate(transactionManager);

        transaccion.executeWithoutResult(status -> {

            publicadorEventos.publishEvent(evento);

            // Simulamos que la operacion fallo.
            status.setRollbackOnly();
        });

        // La transaccion fue revertida.

        List<Notificacion> avisos =
                notificacionRepository
                    .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                            medico.getIdUsuario()
                    );

        assertTrue(
                avisos.isEmpty(),
                "No debe guardarse un aviso de reserva fallida"
        );

        // Comprobar que tampoco se solicito
        // el envio del correo.
        verify(mailService, after(700).never()).send(
                anyString(),
                anyString(),
                anyString()
        );
    }

    @Test
    void dosAltasConcurrentesDeHorariosSuperpuestosSoloPermitenUna() throws Exception {
        Usuario usuarioMedico = crearUsuarioMedico();
        Medico medico = new Medico();
        medico.setUsuario(usuarioMedico);
        medico = medicoRepository.saveAndFlush(medico);

        Long idMedico = medico.getIdMedico();
        LocalDate fecha = LocalDate.now(ZoneId.of("America/Guatemala")).plusDays(3);
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);

        Future<Boolean> primera = ejecutor.submit(() -> {
            inicio.await();
            return crearHorarioOTraslape(idMedico, fecha, LocalTime.of(9, 0), LocalTime.of(10, 0));
        });
        Future<Boolean> segunda = ejecutor.submit(() -> {
            inicio.await();
            return crearHorarioOTraslape(idMedico, fecha, LocalTime.of(9, 30), LocalTime.of(10, 30));
        });

        try {
            inicio.countDown();
            Boolean primeraCreada = primera.get(10, TimeUnit.SECONDS);
            Boolean segundaCreada = segunda.get(10, TimeUnit.SECONDS);

            assertNotEquals(primeraCreada, segundaCreada,
                    "Una alta debe guardarse y la otra detectar el traslape");
            assertEquals(1, horarioDisponibleRepository
                    .findByMedico_IdMedicoAndFecha(idMedico, fecha).size());
        } finally {
            ejecutor.shutdownNow();
        }
    }

    @Test
    void reservaConcurrenteYEdicionSeOrdenanSobreElMismoBloqueo() throws Exception {
        Usuario usuarioMedico = crearUsuarioMedico();
        Medico medico = new Medico();
        medico.setUsuario(usuarioMedico);
        medico = medicoRepository.saveAndFlush(medico);

        Usuario paciente = new Usuario();
        paciente.setNombre("Paciente");
        paciente.setApellido("Prueba");
        paciente.setCorreo("paciente-" + UUID.randomUUID() + "@example.com");
        paciente.setTelefono("55555555");
        paciente.setContraseña("clave-solo-para-pruebas");
        usuarioRepository.saveAndFlush(paciente);

        EstadoCita pendiente = new EstadoCita();
        pendiente.setNombre(EstadoCita.NombreEstado.PENDIENTE);
        pendiente.setDescripcion("Pendiente");
        estadoCitaRepository.saveAndFlush(pendiente);

        LocalDate fechaOriginal = LocalDate.now(ZoneId.of("America/Guatemala")).plusDays(5);
        LocalDate fechaEditada = fechaOriginal.plusDays(1);
        HorarioDisponible horario = new HorarioDisponible();
        horario.setMedico(medico);
        horario.setFecha(fechaOriginal);
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(10, 0));
        horario = horarioDisponibleRepository.saveAndFlush(horario);

        Long idHorario = horario.getIdHorario();
        Long idMedico = medico.getIdMedico();
        AppointmentCreateRequestDTO solicitud = new AppointmentCreateRequestDTO();
        solicitud.setIdHorario(idHorario);
        solicitud.setMotivo("Consulta de prueba");
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);

        Future<AppointmentResponseDTO> reserva = ejecutor.submit(() -> {
            inicio.await();
            return appointmentService.agendarCita(solicitud, paciente.getCorreo());
        });
        Future<Boolean> edicion = ejecutor.submit(() -> {
            inicio.await();
            try {
                scheduleService.actualizarHorarioAtomico(
                        idHorario, idMedico, fechaEditada,
                        LocalTime.of(11, 0), LocalTime.of(12, 0));
                return true;
            } catch (ScheduleConflictException esperado) {
                return false;
            }
        });

        try {
            inicio.countDown();
            AppointmentResponseDTO cita = reserva.get(10, TimeUnit.SECONDS);
            boolean horarioEditado = edicion.get(10, TimeUnit.SECONDS);
            LocalDate fechaFinal = horarioDisponibleRepository.findById(idHorario)
                    .orElseThrow().getFecha();

            assertEquals(horarioEditado ? fechaEditada : fechaOriginal, fechaFinal);
            assertEquals(fechaFinal, cita.getFecha());
            assertTrue(citaRepository.findByHorario_IdHorarioOrderByFechaCreacionDesc(idHorario).isPresent());
        } finally {
            ejecutor.shutdownNow();
        }
    }

    @Test
    void desactivacionesConcurrentesNoEliminanAlUltimoAdministrador() throws Exception {
        Rol rolAdmin = rolRepository.findByNombre("ROLE_ADMIN").orElseThrow();
        Usuario primero = crearAdministrador(rolAdmin);
        Usuario segundo = crearAdministrador(rolAdmin);
        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService ejecutor = Executors.newFixedThreadPool(2);

        Future<Boolean> primeraDesactivacion = ejecutor.submit(() -> {
            inicio.await();
            return desactivarSiEsSeguro(primero.getIdUsuario());
        });
        Future<Boolean> segundaDesactivacion = ejecutor.submit(() -> {
            inicio.await();
            return desactivarSiEsSeguro(segundo.getIdUsuario());
        });

        try {
            inicio.countDown();
            Boolean primeraDesactivada = primeraDesactivacion.get(10, TimeUnit.SECONDS);
            Boolean segundaDesactivada = segundaDesactivacion.get(10, TimeUnit.SECONDS);

            assertNotEquals(primeraDesactivada, segundaDesactivada,
                    "Una desactivación debe rechazarse al quedar solo un administrador");
            assertEquals(1, usuarioRepository.countByActivoTrueAndRoles_Nombre("ROLE_ADMIN"));
        } finally {
            ejecutor.shutdownNow();
        }
    }

    private boolean crearHorarioOTraslape(
            Long idMedico,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {
        try {
            scheduleService.crearHorarioAtomico(idMedico, fecha, horaInicio, horaFin);
            return true;
        } catch (ScheduleConflictException esperado) {
            return false;
        }
    }

    private Usuario crearAdministrador(Rol rolAdmin) {
        Usuario administrador = new Usuario();
        administrador.setNombre("Admin");
        administrador.setApellido("Prueba");
        administrador.setCorreo("admin-" + UUID.randomUUID() + "@example.com");
        administrador.setTelefono("55555555");
        administrador.setContraseña("clave-solo-para-pruebas");
        administrador.getRoles().add(rolAdmin);
        return usuarioRepository.saveAndFlush(administrador);
    }

    private boolean desactivarSiEsSeguro(Long idUsuario) {
        try {
            usuarioService.desactivar(idUsuario);
            return true;
        } catch (ResourceConflictException esperado) {
            return false;
        }
    }
}
