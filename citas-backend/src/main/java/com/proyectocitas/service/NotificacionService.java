package com.proyectocitas.service;

import com.proyectocitas.dto.response.NotificacionResponseDTO;
import com.proyectocitas.model.Notificacion;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.NotificacionRepository;
import com.proyectocitas.repository.UsuarioRepository;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    public NotificacionService(
            NotificacionRepository notificacionRepository,
            UsuarioRepository usuarioRepository) {

        this.notificacionRepository = notificacionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ==============================================
    // OBTENER USUARIO AUTENTICADO
    // ==============================================

    private Usuario obtenerUsuario(String correo) {

        return usuarioRepository.findByCorreo(correo)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Usuario autenticado no encontrado"
                        )
                );
    }

    // ==============================================
    // LISTAR MIS NOTIFICACIONES
    // ==============================================

    @Transactional(readOnly = true)
    public List<NotificacionResponseDTO> listarMias(
            String correo) {

        Usuario usuario = obtenerUsuario(correo);

        return notificacionRepository
                .findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                        usuario.getIdUsuario()
                )
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    // ==============================================
    // MARCAR MI NOTIFICACION COMO LEIDA
    // ==============================================

    @Transactional
    public NotificacionResponseDTO marcarComoLeida(
            Long idNotificacion,
            String correo) {

        Usuario usuario = obtenerUsuario(correo);

        // Se consulta por ID y propietario a la vez.
        // Asi nadie puede modificar avisos ajenos.
        Notificacion notificacion = notificacionRepository
                .findByIdNotificacionAndDestinatario_IdUsuario(
                        idNotificacion,
                        usuario.getIdUsuario()
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Notificacion no encontrada"
                        )
                );

        if (!notificacion.isLeida()) {
            notificacion.setLeida(true);
            notificacionRepository.save(notificacion);
        }

        return convertirDTO(notificacion);
    }

    // ==============================================
    // CONTAR NOTIFICACIONES NO LEIDAS
    // ==============================================

    @Transactional(readOnly = true)
    public long contarNoLeidas(String correo) {

        Usuario usuario = obtenerUsuario(correo);

        return notificacionRepository
                .countByDestinatario_IdUsuarioAndLeidaFalse(
                        usuario.getIdUsuario()
                );
    }

    // ==============================================
    // CONVERTIR ENTIDAD A DTO
    // ==============================================

    private NotificacionResponseDTO convertirDTO(
            Notificacion notificacion) {

        return new NotificacionResponseDTO(
                notificacion.getIdNotificacion(),
                notificacion.getTitulo(),
                notificacion.getMensaje(),
                notificacion.getTipo().name(),
                notificacion.isLeida(),
                notificacion.getFechaCreacion()
        );
    }
}