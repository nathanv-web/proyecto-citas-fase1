package com.proyectocitas.repository;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita.NombreEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPaciente_IdUsuario(Long idUsuario);

    List<Cita> findByEstado_Nombre(NombreEstado nombre);

    Optional<Cita> findByHorario_IdHorario(Long idHorario);

    boolean existsByHorario_IdHorario(Long idHorario);

    List<Cita> findByHorario_Medico_Usuario_IdUsuario(Long idUsuario);
}
