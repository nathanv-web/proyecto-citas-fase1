package com.proyectocitas.controller;

import com.proyectocitas.dto.request.PasswordForgotRequestDTO;
import com.proyectocitas.dto.request.PasswordResetRequestDTO;
import com.proyectocitas.dto.response.IdentityMessageResponseDTO;
import com.proyectocitas.service.PasswordRecoveryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordRecoveryController {
    public static final String GENERIC_MESSAGE =
            "Si el correo corresponde a una cuenta activa, recibirás un enlace para restablecer tu contraseña.";
    private final PasswordRecoveryService recovery;

    public PasswordRecoveryController(PasswordRecoveryService recovery) { this.recovery = recovery; }

    @PostMapping("/forgot")
    public IdentityMessageResponseDTO forgot(@Valid @RequestBody PasswordForgotRequestDTO request,
            HttpServletRequest http) {
        recovery.requestRecovery(request.getEmail(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO(GENERIC_MESSAGE);
    }

    @PostMapping("/reset")
    public IdentityMessageResponseDTO reset(@Valid @RequestBody PasswordResetRequestDTO request,
            HttpServletRequest http) {
        recovery.resetPassword(request.getToken(), request.getNueva(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO("Contraseña restablecida. Inicia sesión con tu nueva contraseña.");
    }
}
