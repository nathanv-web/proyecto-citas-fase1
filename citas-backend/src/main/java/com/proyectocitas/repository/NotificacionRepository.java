package com.proyectocitas.repository;

import com.proyectocitas.model.Notificacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificacionRepository
        extends JpaRepository<Notificacion, Long> {

    // Listar notificaciones del usuario autenticado
    List<Notificacion>
            findByDestinatario_IdUsuarioOrderByFechaCreacionDesc(
                    Long idUsuario
            );

    // Obtener una notificacion solo si pertenece al usuario
    Optional<Notificacion>
            findByIdNotificacionAndDestinatario_IdUsuario(
                    Long idNotificacion,
                    Long idUsuario
            );

    // Contar notificaciones pendientes de leer
    long countByDestinatario_IdUsuarioAndLeidaFalse(
            Long idUsuario
    );
}