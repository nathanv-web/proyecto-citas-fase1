package com.proyectocitas.repository;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita.NombreEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository
        extends JpaRepository<Cita, Long> {

    //Buscar citas de un paciente
    
    List<Cita> findByPaciente_IdUsuario(Long idUsuario);

    //Buscar citas por el estado de la cita
    
    List<Cita> findByEstado_Nombre(NombreEstado nombre);
    
    //Buscar la cita por horario
    
    Optional<Cita> findByHorario_IdHorario(Long idHorario);
    
    //Saber si un horario ya fue reservado

    boolean existsByHorario_IdHorario(Long idHorario);
}