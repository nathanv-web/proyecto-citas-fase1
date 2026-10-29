package com.proyectocitas.service;

import com.proyectocitas.dto.response.EstadisticaResponseDTO;
import com.proyectocitas.repository.CitaRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.repository.UsuarioRepository;
import java.time.YearMonth;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EstadisticaService {
    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final CitaRepository citaRepository;
    private final RolRepository rolRepository;

    public EstadisticaService(UsuarioRepository usuarioRepository, MedicoRepository medicoRepository,
            CitaRepository citaRepository, RolRepository rolRepository) {
        this.usuarioRepository = usuarioRepository;
        this.medicoRepository = medicoRepository;
        this.citaRepository = citaRepository;
        this.rolRepository = rolRepository;
    }

    @Transactional(readOnly = true)
    public EstadisticaResponseDTO obtenerResumen(YearMonth mes) {
        return new EstadisticaResponseDTO(
                usuarioRepository.count(),
                medicoRepository.count(),
                citaRepository.countByHorario_FechaBetween(mes.atDay(1), mes.atEndOfMonth()),
                rolRepository.count(),
                mes.toString());
    }
}
