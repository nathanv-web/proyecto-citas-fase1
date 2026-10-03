package com.proyectocitas.repository;

import com.proyectocitas.model.EstadoCita;
import com.proyectocitas.model.EstadoCita.NombreEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EstadoCitaRepository
        extends JpaRepository<EstadoCita, Long> {

    Optional<EstadoCita> findByNombre(NombreEstado nombre);

}