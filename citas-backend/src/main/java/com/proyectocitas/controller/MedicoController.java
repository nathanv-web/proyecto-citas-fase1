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
import com.proyectocitas.dto.MedicoUpdateDTO;

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

        this.medicoRepository =
                medicoRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.especialidadRepository =
                especialidadRepository;

        this.medicoMapper =
                medicoMapper;
    }


    // =================================================
    // LISTAR MÉDICOS
    // =================================================

    @GetMapping
    public List<MedicoDTO> obtenerMedicos(
            @RequestParam(required = false)
            String especialidad) {

        return medicoRepository
                .findAll()
                .stream()

                .filter(medico ->
                        especialidad == null
                        || especialidad.isBlank()
                        || (
                            medico.getEspecialidad() != null
                            && medico
                                .getEspecialidad()
                                .getNombre()
                                .equalsIgnoreCase(
                                        especialidad
                                )
                        )
                )

                .map(medicoMapper::toDTO)
                .toList();
    }


    // =================================================
    // BUSCAR MÉDICO POR ID
    // =================================================

    @GetMapping("/{id}")
    public ResponseEntity<MedicoDTO>
            obtenerMedicoPorId(
                    @PathVariable Long id) {

        Medico medico =
                medicoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El médico indicado no existe"
                                )
                        );

        return ResponseEntity.ok(
                medicoMapper.toDTO(medico)
        );
    }


    // =================================================
    // CREAR MÉDICO
    // =================================================

    @PostMapping
    public ResponseEntity<MedicoDTO>
            crearMedico(
                    @Valid
                    @RequestBody
                    MedicoRequestDTO request) {


        // ---------------------------------------------
        // VALIDAR USUARIO DUPLICADO
        // ---------------------------------------------

        if (medicoRepository
                .findByUsuario_IdUsuario(
                        request.getIdUsuario()
                )
                .isPresent()) {

            throw new DuplicateResourceException(
                    "El usuario ya tiene un perfil de médico"
            );
        }


        // ---------------------------------------------
        // VALIDAR COLEGIADO DUPLICADO
        // ---------------------------------------------

        String colegiado =
                request
                        .getColegiado()
                        .trim();

        if (medicoRepository
                .findByColegiadoIgnoreCase(
                        colegiado
                )
                .isPresent()) {

            throw new DuplicateResourceException(
                    "Ya existe un médico con ese número de colegiado"
            );
        }


        // ---------------------------------------------
        // BUSCAR USUARIO
        // ---------------------------------------------

        Usuario usuario =
                usuarioRepository
                        .findById(
                                request.getIdUsuario()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El usuario indicado no existe"
                                )
                        );


        // ---------------------------------------------
        // BUSCAR ESPECIALIDAD
        // ---------------------------------------------

        Especialidad especialidad =
                especialidadRepository
                        .findById(
                                request.getIdEspecialidad()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "La especialidad indicada no existe"
                                )
                        );


        // ---------------------------------------------
        // DTO -> ENTIDAD
        // ---------------------------------------------

        Medico medico =
                medicoMapper.toEntity(request);

        medico.setColegiado(colegiado);
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
                        medicoMapper.toDTO(
                                guardado
                        )
                );
    }
            
            //====================================
            // ACTUALIZAR MEDICO
            //====================================


@PutMapping("/{id}")
public ResponseEntity<MedicoDTO> actualizarMedico(
        @PathVariable Long id,
        @Valid @RequestBody MedicoUpdateDTO request) {

    Medico medico = medicoRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                    "El médico indicado no existe"));

    if (request.idUsuario() != null) {
        medicoRepository.findByUsuario_IdUsuario(request.idUsuario())
                .ifPresent(existente -> {
                    if (!id.equals(existente.getIdMedico())) {
                        throw new DuplicateResourceException(
                                "El usuario ya tiene un perfil de médico");
                    }
                });

        Usuario usuario = usuarioRepository.findById(request.idUsuario())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El usuario indicado no existe"));

        medico.setUsuario(usuario);
    }

    if (request.idEspecialidad() != null) {
        Especialidad especialidad = especialidadRepository
                .findById(request.idEspecialidad())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "La especialidad indicada no existe"));

        medico.setEspecialidad(especialidad);
    }

    if (request.colegiado() != null)
        medico.setColegiado(request.colegiado());

    if (request.aniosExperiencia() != null)
        medico.setAniosExperiencia(request.aniosExperiencia());

    if (request.biografia() != null)
        medico.setBiografia(request.biografia());
    if (request.estado() != null) {
        medico.setEstado(request.estado());
    }

    return ResponseEntity.ok(
            medicoMapper.toDTO(medicoRepository.save(medico)));
}

    // =================================================
    // DESACTIVAR MÉDICO
    // =================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>
            eliminarMedico(
                    @PathVariable Long id) {

        Medico medico =
                medicoRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "El médico indicado no existe"
                                )
                        );


        /*
         * No eliminamos físicamente al médico porque
         * puede tener horarios y citas asociadas.
         */
        medico.setEstado(
                Medico.EstadoMedico.INACTIVO
        );

        medicoRepository.save(medico);


        return ResponseEntity
                .noContent()
                .build();
    }
}