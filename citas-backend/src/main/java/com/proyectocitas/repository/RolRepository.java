package com.proyectocitas.repository;

import com.proyectocitas.model.Rol;
import jakarta.persistence.LockModeType;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    // Buscar un rol por su nombre exacto.
    Optional<Rol> findByNombre(String nombre);

    // Punto común de serialización al desactivar administradores.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Rol r WHERE r.nombre = :nombre")
    Optional<Rol> buscarPorNombreConBloqueo(@Param("nombre") String nombre);

    // Validar si ya existe un rol con el mismo nombre.
    boolean existsByNombre(String nombre);

    // =================================================
    // BUSCAR ROLES POR NOMBRE CON PAGINACIÓN
    // =================================================
    // Permite realizar búsquedas parciales sin distinguir
    // entre mayúsculas y minúsculas.
    //
    // Ejemplo:
    // search = "admin" puede encontrar "ROLE_ADMIN".
    Page<Rol> findByNombreContainingIgnoreCase(
            String search,
            Pageable pageable
    );
}
