package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


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

@Tag(name = "Médicos", description = "Administración de perfiles médicos, especialidades y estado de disponibilidad.")
@SecurityRequirement(name = "bearerAuth")
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

@Operation(
    summary = "Listar médicos",
    description = "Devuelve los perfiles médicos con filtros opcionales de búsqueda, especialidad y estado. " +
        "Requiere el permiso VER_MEDICOS.",
    parameters = {
        @Parameter(name = "search", in = ParameterIn.QUERY,
            description = "Texto de búsqueda del médico.", example = "Ana"),
        @Parameter(name = "especialidad", in = ParameterIn.QUERY,
            description = "Texto de la especialidad por el cual filtrar.", example = "Cardiología"),
        @Parameter(name = "estado", in = ParameterIn.QUERY,
            description = "Estado del perfil médico.", example = "ACTIVO",
            schema = @Schema(allowableValues = {"ACTIVO", "INACTIVO"}))
    },
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = MedicoResponseDTO.class)))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
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

    @Operation(
        summary = "Consultar médico por ID",
        description = "Obtiene los datos del perfil médico y sus asociaciones por identificador. Requiere el permiso " +
            "VER_MEDICOS.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador del médico.", example = "1", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = MedicoResponseDTO.class))),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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

    @Operation(
        summary = "Crear perfil de médico",
        description = "Crea un perfil activo para un usuario existente, lo asocia con una especialidad y asigna el rol " +
            "ROLE_DOCTOR. El usuario y el número de colegiado no deben estar asociados a otro perfil. " +
            "Requiere el permiso CREAR_MEDICOS.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Identificadores existentes de usuario y especialidad, colegiado con formato COL-0000, años de " +
                "experiencia no negativos y biografía opcional.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"idUsuario\": 1, \"idEspecialidad\": 1, \"colegiado\": \"COL-1234\", \"aniosExperiencia\": 5, \"biografia\": \"Médico especialista en cardiología.\"}"))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Registro creado correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = MedicoResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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


@Operation(
    summary = "Actualizar médico",
    description = "Actualiza los campos proporcionados del perfil médico, incluida su especialidad o estado. " +
        "Requiere el permiso ACTUALIZAR_MEDICOS.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del médico.", example = "1", required = true)
    },
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Campos del perfil que se desean modificar; los omitidos conservan su valor.",
        required = true, useParameterTypeSchema = true,
        content = @Content(mediaType = "application/json", examples = @ExampleObject(
            name = "Ejemplo", value = "{\"idEspecialidad\": 1, \"colegiado\": \"COL-1234\", \"aniosExperiencia\": 6, \"biografia\": \"Médico especialista en cardiología.\", \"estado\": \"ACTIVO\"}"))),
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MedicoResponseDTO.class))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
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

    @Operation(
        summary = "Desactivar médico",
        description = "Cambia el estado del perfil a INACTIVO sin borrar el registro. Requiere el permiso " +
            "ELIMINAR_MEDICOS.",
        parameters = {
            @Parameter(name = "id", in = ParameterIn.PATH,
                description = "Identificador del médico.", example = "1", required = true)
        },
        responses = {
            @ApiResponse(responseCode = "204", description = "Operación realizada correctamente, sin contenido de respuesta.", content = @Content),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
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
