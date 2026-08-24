package com.proyectocitas.service;

import com.proyectocitas.model.HorarioDisponible;
import com.proyectocitas.repository.HorarioDisponibleRepository;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class HorarioDisponibleService {

    private final HorarioDisponibleRepository horarioRepository;

    public HorarioDisponibleService(
            HorarioDisponibleRepository horarioRepository) {

        this.horarioRepository = horarioRepository;
    }

    public List<HorarioDisponible> obtenerHorariosDisponibles(
            Long medicoId,
            LocalDate fecha) {

        return horarioRepository
                .findByMedico_IdAndFechaAndDisponibleTrueOrderByHoraInicioAsc(
                        medicoId,
                        fecha
                );
    }
}