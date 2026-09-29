package com.proyectocitas.controller;

import com.proyectocitas.dto.MedicoDTO;
import com.proyectocitas.dto.MedicoRequestDTO;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.mapper.MedicoMapper;
import com.proyectocitas.model.Especialidad;
import com.proyectocitas.model.Medico;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.UsuarioRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/v1/medicos")
public class MedicoController {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final MedicoMapper medicoMapper;

    public MedicoController(
            MedicoRepository medicoRepository,
            UsuarioRepository usuarioRepository,
            EspecialidadRepository especialidadRepository,
            MedicoMapper medicoMapper) {

        this.medicoRepository = medicoRepository;
        this.usuarioRepository = usuarioRepository;
        this.especialidadRepository = especialidadRepository;
        this.medicoMapper = medicoMapper;
    }

    @GetMapping
    public List<MedicoDTO> obtenerMedicos(
            @RequestParam(required = false)
            String especialidad) {

        return medicoRepository.findAll()
                .stream()
                .filter(m ->
                        especialidad == null
                        || especialidad.isBlank()
                        || (
                            m.getEspecialidad() != null
                            && m.getEspecialidad()
                                .getNombre()
                                .equalsIgnoreCase(especialidad)
                        )
                )
                .map(medicoMapper::toDTO)
                .toList();
    }

    @PostMapping
    public ResponseEntity<MedicoDTO> crearMedico(
            @Valid
            @RequestBody
            MedicoRequestDTO request) {

        if (medicoRepository
                .findByUsuario_IdUsuario(
                        request.getIdUsuario())
                .isPresent()) {
            
            throw new DuplicateResourceException(
            "El usuario ya tiene un perfil de médico"
            );
        }

        Usuario usuario =
                usuarioRepository
                        .findById(request.getIdUsuario())
                        .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "El usuario indicado no existe"
                        )
                        
                        );

        Especialidad especialidad =
                especialidadRepository
                        .findById(request.getIdEspecialidad())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                "La especialidad indicada no existe")
                                );
    

        Medico medico =
                medicoMapper.toEntity(request);

        medico.setUsuario(usuario);
        medico.setEspecialidad(especialidad);
        medico.setEstado(
                Medico.EstadoMedico.ACTIVO
        );

        Medico guardado =
                medicoRepository.save(medico);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        medicoMapper.toDTO(guardado)
                );
    }
}