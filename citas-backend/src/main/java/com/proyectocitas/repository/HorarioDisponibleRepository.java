package com.proyectocitas.repository;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.model.HorarioDisponible.EstadoHorario;

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
}