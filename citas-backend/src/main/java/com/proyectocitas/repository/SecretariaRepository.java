package com.proyectocitas.repository;

import com.proyectocitas.model.Secretaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SecretariaRepository
        extends JpaRepository<Secretaria, Long> {

    Optional<Secretaria> findByUsuario_IdUsuario(Long idUsuario);

}
