package com.proyectocitas.service;

import com.proyectocitas.dto.request.UsuarioUpdateRequestDTO;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.Impl.UsuarioServiceImpl;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UsuarioEmailVerificationTest {
    @Test
    void conservaVerificacionAlEditarOtrosDatosYLaRevocaAlCambiarElCorreo() {
        var repository = mock(UsuarioRepository.class);
        var user = new Usuario();
        user.setCorreo("ana@example.com");
        user.setCorreoVerificado(true);
        when(repository.findById(7L)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
        var service = new UsuarioServiceImpl(repository, mock(PasswordEncoder.class), mock(RolRepository.class));
        var update = new UsuarioUpdateRequestDTO();
        update.setCorreo("ana@example.com");
        update.setTelefono("55553333");
        service.actualizar(7L, update);
        assertTrue(user.isCorreoVerificado());
        update.setCorreo("otra@example.com");
        service.actualizar(7L, update);
        assertFalse(user.isCorreoVerificado());
    }
}
