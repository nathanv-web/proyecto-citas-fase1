package com.proyectocitas.repository;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita.NombreEstado;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


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
    
    //Buscar agenda del médico autenticado
    
@Query("""
        SELECT c
        FROM Cita c
        JOIN c.horario h
        JOIN h.medico m
        JOIN m.usuario u
        WHERE u.correo = :correo
        AND h.fecha = :fecha
        ORDER BY h.horaInicio ASC
        """)
List<Cita> findAgendaByCorreoMedicoAndFecha(
        @Param("correo") String correo,
        @Param("fecha") LocalDate fecha
);
}