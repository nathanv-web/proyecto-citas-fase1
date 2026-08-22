package com.proyectocitas.repository; 
import com.proyectocitas.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    
    // Spring Boot es tan inteligente que si le pides "buscar por nombre", él hace el resto
    Optional<Rol> findByNombre(String nombre);
}