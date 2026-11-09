package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.proyectocitas.dto.request.PasswordForgotRequestDTO;
import com.proyectocitas.dto.request.PasswordResetRequestDTO;
import com.proyectocitas.dto.response.IdentityMessageResponseDTO;
import com.proyectocitas.service.PasswordRecoveryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Recuperación de contraseña", description = "Solicitud y uso de enlaces públicos para restablecer contraseñas.")
@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordRecoveryController {
    public static final String GENERIC_MESSAGE =
            "Si el correo corresponde a una cuenta activa, recibirás un enlace para restablecer tu contraseña.";
    private final PasswordRecoveryService recovery;

    public PasswordRecoveryController(PasswordRecoveryService recovery) { this.recovery = recovery; }

    @Operation(
        summary = "Solicitar recuperación de contraseña",
        description = "Solicita un enlace de recuperación por correo. Devuelve un mensaje genérico para preservar la " +
            "privacidad de la cuenta y no expone el token en la respuesta.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Correo de la cuenta que solicita recuperar el acceso.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"email\": \"ana@example.com\"}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentityMessageResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "429", ref = "#/components/responses/Error429"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping("/forgot")
    public IdentityMessageResponseDTO forgot(@Valid @RequestBody PasswordForgotRequestDTO request,
            @Parameter(hidden = true) HttpServletRequest http) {
        recovery.requestRecovery(request.getEmail(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO(GENERIC_MESSAGE);
    }

    @Operation(
        summary = "Restablecer contraseña",
        description = "Valida el token recibido por correo, cambia la contraseña e invalida las sesiones anteriores. " +
            "El token es de un solo uso y debe estar vigente.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token de recuperación de 43 caracteres y nueva contraseña de 8 a 64 caracteres. Sustituir el " +
                "token ficticio del ejemplo por el enlace recibido.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"token\": \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\", \"nueva\": \"OtraClave123\"}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentityMessageResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "429", ref = "#/components/responses/Error429"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping("/reset")
    public IdentityMessageResponseDTO reset(@Valid @RequestBody PasswordResetRequestDTO request,
            @Parameter(hidden = true) HttpServletRequest http) {
        recovery.resetPassword(request.getToken(), request.getNueva(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO("Contraseña restablecida. Inicia sesión con tu nueva contraseña.");
    }
}
