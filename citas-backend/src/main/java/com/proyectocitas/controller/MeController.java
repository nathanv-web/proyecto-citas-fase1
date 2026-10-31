package com.proyectocitas.controller;

import com.proyectocitas.dto.request.PasswordChangeRequestDTO;
import com.proyectocitas.dto.response.LoginUsuarioResponseDTO;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.service.AutenticacionService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final AutenticacionService service;
    public MeController(AutenticacionService service) { this.service = service; }

    @GetMapping
    public Map<String, Object> obtener(@AuthenticationPrincipal Usuario usuario) {
        Set<String> roles = usuario.getRoles().stream().map(r -> r.getNombre()).collect(Collectors.toSet());
        Set<String> permisos = usuario.getAuthorities().stream().map(a -> a.getAuthority())
                .filter(a -> !a.startsWith("ROLE_")).collect(Collectors.toSet());
        var profile = new LoginUsuarioResponseDTO(usuario.getIdUsuario(), usuario.getNombre(),
                usuario.getApellido(), usuario.getCorreo(), roles, permisos);
        profile.setCorreoVerificado(usuario.isCorreoVerificado());
        profile.setTelefono(usuario.getTelefono());
        return Map.of("usuario", profile,
                "debeCambiarPassword", usuario.isDebeCambiarPassword());
    }

    @PutMapping("/password")
    public Map<String, String> cambiar(@AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PasswordChangeRequestDTO request) {
        service.cambiarPassword(usuario.getCorreo(), request);
        return Map.of("message", "Contraseña actualizada. Inicia sesión nuevamente.");
    }
}
