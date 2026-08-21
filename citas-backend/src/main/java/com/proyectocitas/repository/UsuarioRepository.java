package com.proyectocitas.repository;

import com.proyectocitas.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Usaremos esto para el Login: buscar si existe un usuario con un email específico
    Optional<Usuario> findByEmail(String email);
}