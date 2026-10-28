package com.proyectocitas.repository;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HorarioDisponibleRepository
        extends JpaRepository<HorarioDisponible, Long> {

    //Buscar horarios de un medico
    List<HorarioDisponible> findByMedico_IdMedico(Long idMedico);

    //Buscar horarios de un medico en una fecha
    List<HorarioDisponible> findByMedico_IdMedicoAndFecha(
            Long idMedico,
            LocalDate fecha
    );

    //Buscar horarios por estado
    List<HorarioDisponible> findByEstado(EstadoHorario estado);

    //Buscar horarios de un medico por fecha y estado
    List<HorarioDisponible> findByMedico_IdMedicoAndFechaAndEstado(
            Long idMedico,
            LocalDate fecha,
            EstadoHorario estado
    );
    // Bloquear un horario durante una reserva
// para evitar reservas simultaneas.

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("""
       SELECT h
       FROM HorarioDisponible h
       WHERE h.idHorario = :idHorario
       """)
Optional<HorarioDisponible> buscarHorarioConBloqueo(
        @Param("idHorario") Long idHorario);
}