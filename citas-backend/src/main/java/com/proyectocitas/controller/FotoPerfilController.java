package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.proyectocitas.model.Usuario;
import com.proyectocitas.service.FotoPerfilService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Foto de perfil", description = "Consulta, carga y eliminación de la foto del usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/me/photo")
public class FotoPerfilController {
    private final FotoPerfilService fotos;

    public FotoPerfilController(FotoPerfilService fotos) { this.fotos = fotos; }

    @Operation(
        summary = "Consultar mi foto de perfil",
        description = "Devuelve la imagen JPG o PNG del usuario autenticado. Si no tiene foto, devuelve 204 sin " +
            "contenido. Requiere un JWT válido.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = {
                    @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary")),
                    @Content(mediaType = "image/png", schema = @Schema(type = "string", format = "binary"))}),
            @ApiResponse(responseCode = "204", description = "El usuario no tiene foto de perfil.", content = @Content),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @GetMapping
    public ResponseEntity<byte[]> obtener(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        return fotos.obtener(usuario.getIdUsuario())
                .map(foto -> ResponseEntity.ok()
                        .cacheControl(CacheControl.noStore())
                        .header("X-Content-Type-Options", "nosniff")
                        .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                        .body(foto.getContenido()))
                .orElseGet(() -> ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build());
    }

    @Operation(
        summary = "Guardar o reemplazar mi foto de perfil",
        description = "Guarda o reemplaza la foto del usuario autenticado. Se envía un archivo en el campo foto " +
            "mediante multipart/form-data; se admiten JPG y PNG de hasta 1 MiB y 4096 píxeles por lado. " +
            "Requiere un JWT válido.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Archivo JPG o PNG en el campo foto: máximo 1 MiB y 4096 píxeles por lado.",
            required = true, useParameterTypeSchema = true),
        responses = {
            @ApiResponse(responseCode = "204", description = "Operación realizada correctamente, sin contenido de respuesta.", content = @Content),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "413", ref = "#/components/responses/Error413"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> guardar(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario,
            @RequestPart("foto") MultipartFile archivo) {
        fotos.guardar(usuario.getIdUsuario(), archivo);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Eliminar mi foto de perfil",
        description = "Elimina la foto del usuario autenticado y devuelve 204 sin contenido. Requiere un JWT válido.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Operación realizada correctamente, sin contenido de respuesta.", content = @Content),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @DeleteMapping
    public ResponseEntity<Void> eliminar(@Parameter(hidden = true) @AuthenticationPrincipal Usuario usuario) {
        fotos.eliminar(usuario.getIdUsuario());
        return ResponseEntity.noContent().build();
    }
}
