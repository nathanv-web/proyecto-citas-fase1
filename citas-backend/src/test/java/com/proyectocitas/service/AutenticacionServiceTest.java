package com.proyectocitas.service;

import com.proyectocitas.dto.request.PasswordChangeRequestDTO;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutenticacionServiceTest {

    private UsuarioRepository usuarioRepository;
    private EntityManager entityManager;
    private PasswordEncoder encoder;
    private AutenticacionService service;
    private Usuario usuario;
    private PasswordChangeRequestDTO request;

    @BeforeEach
    void prepararDatos() {
        usuarioRepository = mock(UsuarioRepository.class);
        entityManager = mock(EntityManager.class);
        encoder = new BCryptPasswordEncoder(4);
        service = new AutenticacionService(usuarioRepository, encoder, entityManager);
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setCorreo("ana@correo.com");
        usuario.setContraseña(encoder.encode("Actual123"));
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.of(usuario));
        request = new PasswordChangeRequestDTO();
        request.setActual("Actual123");
        request.setNueva("NuevaClave123");
    }

    // =================================================
    // AUTENTICAR Y REINICIAR INTENTOS FALLIDOS
    // =================================================

    @Test
    void debeAutenticarYReiniciarLosIntentosTrasUnBloqueoVencido() {
        usuario.setIntentosFallidos(5);
        usuario.setBloqueadoHasta(LocalDateTime.now().minusMinutes(1));

        Usuario resultado = service.autenticar(usuario.getCorreo(), "Actual123");

        assertSame(usuario, resultado);
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        var orden = inOrder(entityManager, usuarioRepository);
        orden.verify(entityManager).refresh(usuario, LockModeType.PESSIMISTIC_WRITE);
        orden.verify(usuarioRepository).save(usuario);
    }

    @Test
    void autenticarDebeRetornar401SiCorreoNoExiste() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.autenticar("desconocido@correo.com", "Actual123"));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(entityManager);
        comprobarQueNoSeGuardoUsuario();
    }

    @Test
    void autenticarDebeRetornar401SiCorreoEsNulo() {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.autenticar(null, "Actual123"));

        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
        verifyNoInteractions(usuarioRepository, entityManager);
    }

    @Test
    void autenticarDebeRegistrarUnFalloSiPasswordEsNulo() {
        comprobarAutenticacionRechazada(null);

        assertEquals(1, usuario.getIntentosFallidos());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void autenticarDebeRechazarUnUsuarioInactivoAunqueLaClaveSeaCorrecta() {
        usuario.setActivo(false);

        comprobarAutenticacionRechazada("Actual123");

        assertEquals(0, usuario.getIntentosFallidos());
        comprobarQueNoSeGuardoUsuario();
    }

    @Test
    void autenticarDebeRechazarUnUsuarioConBloqueoVigente() {
        usuario.setIntentosFallidos(5);
        LocalDateTime bloqueo = LocalDateTime.now().plusMinutes(10);
        usuario.setBloqueadoHasta(bloqueo);

        comprobarAutenticacionRechazada("Actual123");

        assertEquals(5, usuario.getIntentosFallidos());
        assertEquals(bloqueo, usuario.getBloqueadoHasta());
        comprobarQueNoSeGuardoUsuario();
    }

    // =================================================
    // CONTAR INTENTOS Y APLICAR BLOQUEOS TEMPORALES
    // =================================================

    @Test
    void autenticarDebeContarLosFallosSinBloquearAntesDelQuintoIntento() {
        usuario.setIntentosFallidos(3);

        comprobarAutenticacionRechazada("Incorrecta123");

        assertEquals(4, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void autenticarDebeBloquearUnMinutoAlLlegarAlQuintoIntento() {
        usuario.setIntentosFallidos(4);
        LocalDateTime antes = LocalDateTime.now();

        comprobarAutenticacionRechazada("Incorrecta123");

        assertEquals(5, usuario.getIntentosFallidos());
        comprobarDuracionDelBloqueo(antes, 1);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void autenticarDebeLimitarElBloqueoAMaximoSesentaMinutos() {
        usuario.setIntentosFallidos(39);
        LocalDateTime antes = LocalDateTime.now();

        comprobarAutenticacionRechazada("Incorrecta123");

        assertEquals(40, usuario.getIntentosFallidos());
        comprobarDuracionDelBloqueo(antes, 60);
        verify(usuarioRepository).save(usuario);
    }

    // =================================================
    // CAMBIAR PASSWORD E INVALIDAR SESIONES ANTERIORES
    // =================================================

    @Test
    void debeCambiarPasswordCifrarLaNuevaClaveEInvalidarSesionesAnteriores() {
        usuario.setVersionSesion(4L);
        usuario.setDebeCambiarPassword(true);
        usuario.setIntentosFallidos(3);
        usuario.setBloqueadoHasta(LocalDateTime.now().minusMinutes(1));

        service.cambiarPassword(usuario.getCorreo(), request);

        assertTrue(encoder.matches("NuevaClave123", usuario.getPassword()));
        assertFalse(encoder.matches("Actual123", usuario.getPassword()));
        assertNotEquals("NuevaClave123", usuario.getPassword());
        assertEquals(5L, usuario.getVersionSesion());
        assertFalse(usuario.isDebeCambiarPassword());
        assertEquals(0, usuario.getIntentosFallidos());
        assertNull(usuario.getBloqueadoHasta());
        var orden = inOrder(entityManager, usuarioRepository);
        orden.verify(entityManager).refresh(usuario, LockModeType.PESSIMISTIC_WRITE);
        orden.verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPasswordDebeRetornar401SiUsuarioNoExiste() {
        when(usuarioRepository.findByCorreo(usuario.getCorreo())).thenReturn(Optional.empty());

        comprobarCambioRechazado(HttpStatus.UNAUTHORIZED);

        verifyNoInteractions(entityManager);
        comprobarQueNoSeGuardoUsuario();
    }

    @Test
    void cambiarPasswordDebeRechazarUsuariosInactivos() {
        usuario.setActivo(false);

        comprobarCambioRechazado(HttpStatus.UNAUTHORIZED);

        comprobarQueNoSeGuardoUsuario();
    }

    @Test
    void cambiarPasswordDebeRechazarUsuariosBloqueados() {
        usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(10));

        comprobarCambioRechazado(HttpStatus.UNAUTHORIZED);

        comprobarQueNoSeGuardoUsuario();
    }

    @Test
    void cambiarPasswordDebeRegistrarUnFalloSiLaClaveActualEsIncorrecta() {
        request.setActual("Incorrecta123");
        usuario.setIntentosFallidos(4);

        comprobarCambioRechazado(HttpStatus.BAD_REQUEST);

        assertEquals(5, usuario.getIntentosFallidos());
        assertNotNull(usuario.getBloqueadoHasta());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPasswordDebeRechazarLaMismaClaveActual() {
        request.setNueva("Actual123");

        comprobarCambioRechazado(HttpStatus.BAD_REQUEST);

        assertEquals(0, usuario.getIntentosFallidos());
        comprobarQueNoSeGuardoUsuario();
    }

    @Test
    void cambiarPasswordDebeRechazarUnaClaveQueSupereSetentaYDosBytesUtf8() {
        request.setNueva("ñ".repeat(37));

        comprobarCambioRechazado(HttpStatus.BAD_REQUEST);

        comprobarQueNoSeGuardoUsuario();
    }

    @Test
    void cambiarPasswordDebeAceptarUnaClaveDeExactamenteSetentaYDosBytesUtf8() {
        request.setNueva("ñ".repeat(36));

        service.cambiarPassword(usuario.getCorreo(), request);

        assertTrue(encoder.matches("ñ".repeat(36), usuario.getPassword()));
        assertEquals(1L, usuario.getVersionSesion());
        verify(usuarioRepository).save(usuario);
    }

    private void comprobarAutenticacionRechazada(String password) {
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.autenticar(usuario.getCorreo(), password));
        assertEquals(HttpStatus.UNAUTHORIZED, error.getStatusCode());
    }

    private void comprobarCambioRechazado(HttpStatus estado) {
        String passwordAnterior = usuario.getPassword();
        long versionAnterior = usuario.getVersionSesion();
        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.cambiarPassword(usuario.getCorreo(), request));
        assertEquals(estado, error.getStatusCode());
        assertEquals(passwordAnterior, usuario.getPassword());
        assertEquals(versionAnterior, usuario.getVersionSesion());
    }

    private void comprobarDuracionDelBloqueo(LocalDateTime antes, long minutos) {
        LocalDateTime despues = LocalDateTime.now();
        assertNotNull(usuario.getBloqueadoHasta());
        assertFalse(usuario.getBloqueadoHasta().isBefore(antes.plusMinutes(minutos)));
        assertFalse(usuario.getBloqueadoHasta().isAfter(despues.plusMinutes(minutos)));
    }

    private void comprobarQueNoSeGuardoUsuario() {
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
