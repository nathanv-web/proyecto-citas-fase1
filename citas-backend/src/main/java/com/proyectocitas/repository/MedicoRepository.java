package com.proyectocitas.repository;

import com.proyectocitas.model.Medico;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByUsuario_IdUsuario(Long idUsuario);

    Optional<Medico> findByColegiadoIgnoreCase(String colegiado);

    Optional<Medico> findByUsuario_Correo(String correo);

    // Serializa la creación y edición de horarios del mismo médico.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Medico m WHERE m.idMedico = :id")
    Optional<Medico> buscarMedicoConBloqueo(@Param("id") Long id);

    // Buscar médicos por nombre, apellido, correo,
    // especialidad y estado.
    @Query("""
           SELECT m FROM Medico m
           LEFT JOIN m.usuario u
           LEFT JOIN m.especialidad e
           WHERE (
               :search = ''
               OR LOCATE(:search, LOWER(COALESCE(u.nombre, ''))) > 0
               OR LOCATE(:search, LOWER(COALESCE(u.apellido, ''))) > 0
               OR LOCATE(:search, LOWER(COALESCE(u.correo, ''))) > 0
           )
           AND (
               :especialidad = ''
               OR LOWER(COALESCE(e.nombre, '')) = :especialidad
           )
           AND (:estado IS NULL OR m.estado = :estado)
           ORDER BY m.idMedico
           """)
    List<Medico> buscarConFiltros(
            @Param("search") String search,
            @Param("especialidad") String especialidad,
            @Param("estado") Medico.EstadoMedico estado
    );
}
