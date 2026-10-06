package com.proyectocitas.repository;

import com.proyectocitas.model.Especialidad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Long> {

    // Buscar una especialidad sin fallar si existen
    // registros duplicados antiguos.
    Optional<Especialidad> findFirstByNombreIgnoreCase(
            String nombre
    );

    // Verificar si ya existe una especialidad con ese nombre.
    boolean existsByNombreIgnoreCase(
            String nombre
    );

    // Verificar duplicado al actualizar,
    // ignorando la misma especialidad.
    boolean existsByNombreIgnoreCaseAndIdEspecialidadNot(
            String nombre,
            Long idEspecialidad
    );
}