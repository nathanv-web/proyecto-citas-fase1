package com.proyectocitas.service;

import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.dto.request.PasswordChangeRequestDTO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AutenticacionService {
    private static final Logger logger = LoggerFactory.getLogger(AutenticacionService.class);
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;
    private final String hashSimulado;

    public AutenticacionService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
        this.hashSimulado = encoder.encode(java.util.UUID.randomUUID().toString());
    }

    // Conservar el contador incluso cuando se rechazan las credenciales.
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public Usuario autenticar(String correo, String password) {
        Usuario usuario = correo == null ? null : repository.buscarParaAutenticacion(correo).orElse(null);
        boolean coincide = encoder.matches(password == null ? "" : password,
                usuario == null ? hashSimulado : usuario.getPassword());
        if (usuario == null || !usuario.isEnabled() || !usuario.isAccountNonLocked()) {
            logger.warn("Autenticación rechazada");
            throw credencialesInvalidas();
        }
        if (!coincide) {
            registrarFallo(usuario);
            throw credencialesInvalidas();
        }
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        repository.save(usuario);
        logger.info("Autenticación correcta para usuario {}", usuario.getIdUsuario());
        return usuario;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public void cambiarPassword(String correo, PasswordChangeRequestDTO request) {
        Usuario usuario = repository.buscarParaAutenticacion(correo).orElseThrow(this::credencialesInvalidas);
        if (!usuario.isEnabled() || !usuario.isAccountNonLocked()) throw credencialesInvalidas();
        if (!encoder.matches(request.getActual(), usuario.getPassword())) {
            registrarFallo(usuario);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta");
        }
        if (encoder.matches(request.getNueva(), usuario.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nueva contraseña debe ser diferente de la actual");
        }
        if (request.getNueva().getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña supera el límite de 72 bytes UTF-8");
        }
        usuario.setContraseña(encoder.encode(request.getNueva()));
        usuario.setDebeCambiarPassword(false);
        usuario.setIntentosFallidos(0);
        usuario.setBloqueadoHasta(null);
        usuario.setVersionSesion(usuario.getVersionSesion() + 1);
        repository.save(usuario);
        logger.info("Contraseña actualizada y sesiones invalidadas para usuario {}", usuario.getIdUsuario());
    }

    private void registrarFallo(Usuario usuario) {
        int intentos = usuario.getIntentosFallidos() + 1;
        usuario.setIntentosFallidos(intentos);
        if (intentos >= 5) {
            long minutos = Math.min(60, 1L << Math.min(6, (intentos - 5) / 5));
            usuario.setBloqueadoHasta(LocalDateTime.now().plusMinutes(minutos));
        }
        repository.save(usuario);
        logger.warn("Verificación de credenciales rechazada para usuario {}", usuario.getIdUsuario());
    }

    private ResponseStatusException credencialesInvalidas() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas o acceso temporalmente no disponible");
    }
}
