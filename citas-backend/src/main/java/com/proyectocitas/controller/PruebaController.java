package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Prueba de funcionamiento", description = "Comprobación básica de la respuesta del backend.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api")
public class PruebaController {

    @Operation(
        summary = "Comprobar respuesta del backend",
        description = "Devuelve un mensaje de texto que confirma que el backend responde. Esta ruta requiere un JWT " +
            "válido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "text/plain", schema = @Schema(type = "string"))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping("/hola")
    public String saludar() {
        return "¡El backend de Citas está funcionando correctamente!";
    }
}