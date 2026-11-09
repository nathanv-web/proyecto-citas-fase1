package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


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

@Tag(name = "Mi cuenta", description = "Perfil y cambio de contraseña del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/me")
public class MeController {
    private final AutenticacionService service;
    public MeController(AutenticacionService service) { this.service = service; }

    @Operation(
        summary = "Consultar mi perfil",
        description = "Devuelve los datos del usuario autenticado, sus roles, permisos, estado de verificación de " +
            "correo y el indicador debeCambiarPassword. Requiere un JWT válido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/RespuestaMiPerfil"))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping
    public Map<String, Object> obtener(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
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

    @Operation(
        summary = "Cambiar mi contraseña",
        description = "Verifica la contraseña actual y guarda una nueva diferente. Invalida las sesiones existentes; " +
            "se debe iniciar sesión nuevamente. Requiere un JWT válido.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contraseña actual y nueva contraseña de 8 a 64 caracteres, con un máximo de 72 bytes UTF-8.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"actual\": \"EjemploClave123\", \"nueva\": \"OtraClave123\"}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(type = "object", additionalPropertiesSchema = String.class),
                    examples = @ExampleObject(value = "{\"message\":\"Contraseña actualizada. Inicia sesión nuevamente.\"}"))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PutMapping("/password")
    public Map<String, String> cambiar(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario,
            @Valid @RequestBody PasswordChangeRequestDTO request) {
        service.cambiarPassword(usuario.getCorreo(), request);
        return Map.of("message", "Contraseña actualizada. Inicia sesión nuevamente.");
    }
}
