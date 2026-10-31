package com.proyectocitas.repository;

import com.proyectocitas.model.Especialidad;
import com.proyectocitas.model.Medico;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EspecialidadRepository
        extends JpaRepository<Especialidad, Long> {

    @Query("SELECT COUNT(m) > 0 FROM Medico m WHERE m.especialidad.idEspecialidad = :id AND m.estado = :estado")
    boolean tieneMedicosConEstado(@Param("id") Long id, @Param("estado") Medico.EstadoMedico estado);

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
