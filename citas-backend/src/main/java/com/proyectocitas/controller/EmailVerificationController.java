package com.proyectocitas.controller;

import com.proyectocitas.dto.request.EmailVerificationRequestDTO;
import com.proyectocitas.dto.request.EmailVerifyRequestDTO;
import com.proyectocitas.dto.response.IdentityMessageResponseDTO;
import com.proyectocitas.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth/email")
public class EmailVerificationController {
    private final EmailVerificationService verification;
    public EmailVerificationController(EmailVerificationService verification) { this.verification = verification; }

    @PostMapping("/request")
    public IdentityMessageResponseDTO request(@Valid @RequestBody EmailVerificationRequestDTO request,
            HttpServletRequest http) {
        verification.requestVerification(request.getEmail(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO("Si la cuenta necesita verificar su correo, recibirás un enlace de confirmación.");
    }

    @PostMapping("/verify")
    public IdentityMessageResponseDTO verify(@Valid @RequestBody EmailVerifyRequestDTO request,
            HttpServletRequest http) {
        verification.verify(request.getToken(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO("Correo verificado correctamente.");
    }
}
