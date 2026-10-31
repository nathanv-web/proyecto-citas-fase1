package com.proyectocitas.controller;

import java.util.Locale;
import org.springframework.web.server.ResponseStatusException;

import com.proyectocitas.dto.response.MedicoResponseDTO;
import com.proyectocitas.dto.request.MedicoCreateRequestDTO;

import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;

import com.proyectocitas.mapper.MedicoMapper;

import com.proyectocitas.model.Especialidad;
import com.proyectocitas.model.Medico;
import com.proyectocitas.model.Usuario;

import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.repository.MedicoRepository;
import com.proyectocitas.repository.UsuarioRepository;
import com.proyectocitas.service.MedicoService;
import com.proyectocitas.dto.request.MedicoUpdateRequestDTO;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

@RestController
@RequestMapping("/api/v1/medicos")
public class MedicoController {

    private final MedicoRepository medicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final EspecialidadRepository especialidadRepository;
    private final MedicoMapper medicoMapper;
    private final MedicoService medicoService;


    public MedicoController(
            MedicoRepository medicoRepository,
            UsuarioRepository usuarioRepository,
            EspecialidadRepository especialidadRepository,
            MedicoMapper medicoMapper,
            MedicoService medicoService) {

        this.medicoRepository =
                medicoRepository;

        this.usuarioRepository =
                usuarioRepository;

        this.especialidadRepository =
                especialidadRepository;

        this.medicoMapper =
                medicoMapper;

        this.medicoService = medicoService;
    }


    // =================================================
// LISTAR Y BUSCAR MÉDICOS
// =================================================

@GetMapping
public List<MedicoResponseDTO> obtenerMedicos(
        @RequestParam(name = "search", required = false)
        String search,

        @RequestParam(name = "especialidad", required = false)
        String especialidad,

        @RequestParam(name = "estado", required = false)
        String estado) {

    // Normalizar los datos de búsqueda
    String textoBusqueda = normalizar(search);
    String filtroEspecialidad = normalizar(especialidad);

    // Validar el estado del médico
    Medico.EstadoMedico filtroEstado = null;

    if (estado != null && !estado.isBlank()) {
        try {
            filtroEstado = Medico.EstadoMedico.valueOf(
                    estado.trim().toUpperCase(Locale.ROOT)
            );

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El estado debe ser ACTIVO o INACTIVO"
            );
        }
    }

    // Consultar médicos aplicando los filtros
    return medicoRepository.buscarConFiltros(
                    textoBusqueda,
                    filtroEspecialidad,
                    filtroEstado
            )
            .stream()
            .map(medicoMapper::toDTO)
            .toList();
}

// Normalizar textos para las búsquedas
private static String normalizar(String texto) {
    return texto == null
            ? ""
            : texto.trim().toLowerCase(Locale.ROOT);
}


    // =================================================
    // BUSCAR MÉDICO POR ID
    // =================================================

    @GetMapping("/{id}")
    public ResponseEntity<MedicoResponseDTO>
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
    public ResponseEntity<MedicoResponseDTO>
            crearMedico(
                    @Valid
                    @RequestBody
                    MedicoCreateRequestDTO request) {


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


        Medico guardado;
        try {
            guardado = medicoService.crearConRolDoctor(medico);
        } catch (DataIntegrityViolationException ex) {
            if (medicoRepository.findByColegiadoIgnoreCase(colegiado).isPresent()) {
                throw new DuplicateResourceException(
                        "Ya existe un médico con ese número de colegiado");
            }
            if (medicoRepository.findByUsuario_IdUsuario(request.getIdUsuario()).isPresent()) {
                throw new DuplicateResourceException(
                        "El usuario ya tiene un perfil de médico");
            }
            throw ex;
        }


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
public ResponseEntity<MedicoResponseDTO> actualizarMedico(
        @PathVariable Long id,
        @Valid @RequestBody MedicoUpdateRequestDTO request) {

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
