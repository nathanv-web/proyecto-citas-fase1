package com.proyectocitas.repository;

import com.proyectocitas.model.Enfermero;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EnfermeroRepository
        extends JpaRepository<Enfermero, Long> {

    Optional<Enfermero> findByUsuario_IdUsuario(Long idUsuario);

}