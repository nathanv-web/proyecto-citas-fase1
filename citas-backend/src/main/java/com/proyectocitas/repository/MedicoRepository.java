package com.proyectocitas.repository;

import com.proyectocitas.model.Medico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MedicoRepository extends JpaRepository<Medico, Long> {

    Optional<Medico> findByUsuario_IdUsuario(Long idUsuario);
    Optional<Medico> findByColegiadoIgnoreCase(String colegiado);
    Optional<Medico> findByUsuario_Correo(String correo);

}