package com.proyectocitas.repository;

import com.proyectocitas.model.Cita;
import com.proyectocitas.model.EstadoCita.NombreEstado;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


import java.util.List;
import java.util.Optional;

@Repository
public interface CitaRepository
        extends JpaRepository<Cita, Long> {

    // Conteo por fecha de atención, incluyendo citas canceladas conservadas en el historial.
    long countByHorario_FechaBetween(LocalDate desde, LocalDate hasta);

    //Buscar citas de un paciente
    
    List<Cita> findByPaciente_IdUsuario(Long idUsuario);

    //Buscar citas por el estado de la cita
    
    List<Cita> findByEstado_Nombre(NombreEstado nombre);
    
    //Buscar la cita por horario
    
    Optional<Cita> findByHorario_IdHorarioOrderByFechaCreacionDesc(Long idHorario);
    
    //Saber si un horario ya fue reservado
    
    boolean existsByHorario_IdHorario(Long idHorario);

    boolean existsByHorario_IdHorarioAndEstado_NombreNot(Long idHorario, NombreEstado estado);

    @Query("""
           SELECT c.idCita FROM Cita c JOIN c.horario h
           WHERE c.estado.nombre = :estado
           AND c.recordatorioEnviado = false
           AND (h.fecha > :desdeFecha OR (h.fecha = :desdeFecha AND h.horaInicio > :desdeHora))
           AND (h.fecha < :hastaFecha OR (h.fecha = :hastaFecha AND h.horaInicio <= :hastaHora))
           ORDER BY h.fecha, h.horaInicio, c.idCita
           """)
    List<Long> buscarCitasParaRecordatorio(
            @Param("estado") NombreEstado estado,
            @Param("desdeFecha") LocalDate desdeFecha,
            @Param("desdeHora") LocalTime desdeHora,
            @Param("hastaFecha") LocalDate hastaFecha,
            @Param("hastaHora") LocalTime hastaHora,
            Pageable limite);
    
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

// =================================================
// CONSULTAR AGENDA DEL MEDICO ENTRE DOS FECHAS
// =================================================

@Query("""
       SELECT c
       FROM Cita c
       JOIN c.horario h
       JOIN h.medico m
       JOIN m.usuario u
       WHERE u.correo = :correo
       AND h.fecha BETWEEN :desde AND :hasta
       ORDER BY h.fecha ASC, h.horaInicio ASC, c.idCita ASC
       """)
List<Cita> findAgendaByCorreoMedicoAndRango(
        @Param("correo") String correo,
        @Param("desde") LocalDate desde,
        @Param("hasta") LocalDate hasta
);

@Query("""
       SELECT c.horario.idHorario
       FROM Cita c
       WHERE c.idCita = :idCita
       """)
Optional<Long> obtenerIdHorarioDeCita(
        @Param("idCita") Long idCita
);
}
