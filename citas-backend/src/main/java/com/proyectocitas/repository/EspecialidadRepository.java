package com.proyectocitas.repository;

import com.proyectocitas.model.Especialidad;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {
    
    Optional<Especialidad> findByNombre(String nombre);
}