package com.proyectocitas.service;

import com.proyectocitas.model.FotoPerfil;
import com.proyectocitas.repository.FotoPerfilRepository;
import com.proyectocitas.repository.UsuarioRepository;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;
import javax.imageio.ImageIO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FotoPerfilService {
    private static final long MAX_BYTES = 1024L * 1024;
    private final FotoPerfilRepository fotos;
    private final UsuarioRepository usuarios;

    public FotoPerfilService(FotoPerfilRepository fotos, UsuarioRepository usuarios) {
        this.fotos = fotos;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public Optional<FotoPerfil> obtener(Long idUsuario) { return fotos.findById(idUsuario); }

    @Transactional
    public void guardar(Long idUsuario, MultipartFile archivo) {
        if (archivo.isEmpty()) throw invalida("Selecciona una imagen JPG o PNG");
        if (archivo.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La foto no debe superar 1 MiB");
        }
        try {
            byte[] contenido = archivo.getBytes();
            String tipo = validarImagen(contenido);
            FotoPerfil foto = fotos.findById(idUsuario).orElseGet(() -> {
                FotoPerfil nueva = new FotoPerfil();
                nueva.setUsuario(usuarios.getReferenceById(idUsuario));
                return nueva;
            });
            foto.setTipoContenido(tipo);
            foto.setContenido(contenido);
            fotos.save(foto);
        } catch (IOException ex) {
            throw invalida("No se pudo leer la imagen. Selecciona un archivo JPG o PNG válido");
        }
    }

    @Transactional
    public void eliminar(Long idUsuario) { fotos.deleteById(idUsuario); }

    private String validarImagen(byte[] contenido) throws IOException {
        try (var entrada = ImageIO.createImageInputStream(new ByteArrayInputStream(contenido))) {
            if (entrada == null) throw invalida("El archivo no es una imagen válida");
            var lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) throw invalida("Solo se permiten imágenes JPG o PNG");
            var lector = lectores.next();
            try {
                String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
                if (!formato.equals("jpeg") && !formato.equals("png")) {
                    throw invalida("Solo se permiten imágenes JPG o PNG");
                }
                lector.setInput(entrada);
                if (lector.getWidth(0) > 4096 || lector.getHeight(0) > 4096) {
                    throw invalida("La imagen no debe superar 4096 píxeles por lado");
                }
                if (lector.read(0) == null) throw invalida("El archivo no es una imagen válida");
                return formato.equals("png") ? "image/png" : "image/jpeg";
            } finally {
                lector.dispose();
            }
        }
    }

    private ResponseStatusException invalida(String mensaje) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensaje);
    }
}
