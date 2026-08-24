package com.proyectocitas.repository;

import com.proyectocitas.model.CitaMedica;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CitaMedicaRepository extends JpaRepository<CitaMedica, Long> {

    boolean existsByIdHorario(Long idHorario);
}
