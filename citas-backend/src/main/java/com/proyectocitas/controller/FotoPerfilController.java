package com.proyectocitas.controller;

import com.proyectocitas.model.Usuario;
import com.proyectocitas.service.FotoPerfilService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/me/photo")
public class FotoPerfilController {
    private final FotoPerfilService fotos;

    public FotoPerfilController(FotoPerfilService fotos) { this.fotos = fotos; }

    @GetMapping
    public ResponseEntity<byte[]> obtener(@AuthenticationPrincipal Usuario usuario) {
        return fotos.obtener(usuario.getIdUsuario())
                .map(foto -> ResponseEntity.ok()
                        .cacheControl(CacheControl.noStore())
                        .header("X-Content-Type-Options", "nosniff")
                        .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                        .body(foto.getContenido()))
                .orElseGet(() -> ResponseEntity.noContent().cacheControl(CacheControl.noStore()).build());
    }

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> guardar(@AuthenticationPrincipal Usuario usuario,
            @RequestPart("foto") MultipartFile archivo) {
        fotos.guardar(usuario.getIdUsuario(), archivo);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminar(@AuthenticationPrincipal Usuario usuario) {
        fotos.eliminar(usuario.getIdUsuario());
        return ResponseEntity.noContent().build();
    }
}
