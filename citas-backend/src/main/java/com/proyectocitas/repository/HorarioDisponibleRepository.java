package com.proyectocitas.repository;

import com.proyectocitas.model.HorarioDisponible;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioDisponibleRepository
        extends JpaRepository<HorarioDisponible, Long> {

    List<HorarioDisponible>
            findByMedico_IdAndFechaAndDisponibleTrueOrderByHoraInicioAsc(
                    Long medicoId,
                    LocalDate fecha
            );
}