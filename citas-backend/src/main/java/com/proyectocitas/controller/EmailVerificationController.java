package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.proyectocitas.dto.request.EmailVerificationRequestDTO;
import com.proyectocitas.dto.request.EmailVerifyRequestDTO;
import com.proyectocitas.dto.response.IdentityMessageResponseDTO;
import com.proyectocitas.service.EmailVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Verificación de correo", description = "Solicitud y confirmación de enlaces públicos de verificación de correo.")
@RestController
@RequestMapping("/api/v1/auth/email")
public class EmailVerificationController {
    private final EmailVerificationService verification;
    public EmailVerificationController(EmailVerificationService verification) { this.verification = verification; }

    @Operation(
        summary = "Solicitar enlace de verificación",
        description = "Solicita el envío de un enlace de confirmación si la cuenta necesita verificar su correo. " +
            "Devuelve un mensaje genérico y no expone el token.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Correo de la cuenta cuya dirección se desea verificar.",
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
    @PostMapping("/request")
    public IdentityMessageResponseDTO request(@Valid @RequestBody EmailVerificationRequestDTO request,
            @Parameter(hidden = true) HttpServletRequest http) {
        verification.requestVerification(request.getEmail(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO("Si la cuenta necesita verificar su correo, recibirás un enlace de confirmación.");
    }

    @Operation(
        summary = "Verificar correo electrónico",
        description = "Confirma la dirección de correo mediante un token vigente de un solo uso. Los tokens de " +
            "recuperación de contraseña no sirven para esta operación.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token de verificación de 43 caracteres recibido por correo. El valor del ejemplo es ficticio.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"token\": \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\"}"))),
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = IdentityMessageResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "429", ref = "#/components/responses/Error429"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping("/verify")
    public IdentityMessageResponseDTO verify(@Valid @RequestBody EmailVerifyRequestDTO request,
            @Parameter(hidden = true) HttpServletRequest http) {
        verification.verify(request.getToken(), http.getRemoteAddr());
        return new IdentityMessageResponseDTO("Correo verificado correctamente.");
    }
}
