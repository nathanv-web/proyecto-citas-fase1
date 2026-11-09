
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


import com.proyectocitas.dto.response.UsuarioResponseDTO;
import com.proyectocitas.dto.request.UsuarioCreateRequestDTO;
import com.proyectocitas.dto.request.UsuarioRolesRequestDTO;
import com.proyectocitas.dto.request.UsuarioUpdateRequestDTO;
import com.proyectocitas.mapper.UsuarioMapper;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.service.UsuarioService;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;


@Tag(name = "Usuarios", description = "Administración de usuarios, filtros, estado y asignación de roles.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/users")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;


    public UsuarioController(
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper) {

        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
    }


    // =================================================
    // CREAR USUARIO
    // =================================================

    @Operation(
        summary = "Crear usuario",
        description = "Crea un usuario desde la administración. La cuenta queda marcada para cambiar su contraseña en " +
            "el siguiente acceso; los roles se asignan mediante la operación correspondiente. Requiere el " +
            "permiso CREAR_USUARIOS.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del nuevo usuario. Nombre y apellido sin espacios; teléfono de 8 a 15 dígitos y " +
                "contraseña de 8 a 64 caracteres.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"nombre\": \"Ana\", \"apellido\": \"Lopez\", \"correo\": \"ana@example.com\", \"telefono\": \"55554444\", \"contrasena\": \"EjemploClave123\"}"))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Registro creado correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(
            @Valid @RequestBody UsuarioCreateRequestDTO request) {

        Usuario usuario =
                usuarioMapper.toEntity(request);

        Usuario usuarioGuardado =
                usuarioService.guardar(usuario);

        UsuarioResponseDTO respuesta =
                usuarioMapper.toDTO(usuarioGuardado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }
   // =================================================
// LISTAR, BUSCAR Y FILTRAR USUARIOS
// =================================================
// Los parámetros son opcionales para mantener compatibilidad
// con las llamadas existentes a GET /api/v1/users.
@Operation(
    summary = "Listar usuarios",
    description = "Devuelve usuarios con filtros opcionales de búsqueda, estado activo y nombre de rol. Requiere " +
        "el permiso VER_USUARIOS.",
    parameters = {
        @Parameter(name = "search", in = ParameterIn.QUERY,
            description = "Texto de búsqueda en los datos del usuario.", example = "Ana"),
        @Parameter(name = "activo", in = ParameterIn.QUERY,
            description = "Filtra cuentas activas (true) o inactivas (false).", example = "true"),
        @Parameter(name = "rol", in = ParameterIn.QUERY,
            description = "Nombre del rol por el cual filtrar.", example = "ROLE_PATIENT")
    },
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UsuarioResponseDTO.class)))),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@GetMapping
public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Boolean activo,
        @RequestParam(required = false) String rol) {

    // Si no se envían filtros, el servicio igualmente devuelve
    // todos los usuarios, manteniendo el comportamiento anterior.
    List<UsuarioResponseDTO> usuarios =
            usuarioService.buscarConFiltros(
                    search,
                    activo,
                    rol
            )
            .stream()
            .map(usuarioMapper::toDTO)
            .toList();

    return ResponseEntity.ok(usuarios);
}
// =================================================
// BUSCAR USUARIO POR ID
// =================================================

@Operation(
    summary = "Consultar usuario por ID",
    description = "Obtiene los datos de un usuario a partir de su identificador. Requiere el permiso VER_USUARIOS.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del usuario.", example = "1", required = true)
    },
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@GetMapping("/{id}")
public ResponseEntity<UsuarioResponseDTO> buscarUsuarioPorId(
        @PathVariable Long id) {

    Usuario usuario =
            usuarioService.obtenerPorId(id);

    UsuarioResponseDTO respuesta =
            usuarioMapper.toDTO(usuario);

    return ResponseEntity
            .ok(respuesta);
}
// =================================================
// Actualizar datos
// =================================================
@Operation(
    summary = "Actualizar usuario",
    description = "Actualiza los campos proporcionados del usuario. Cambiar el correo revoca su verificación; los " +
        "cambios de estado respetan las restricciones de administración. Requiere el permiso " +
        "ACTUALIZAR_USUARIOS.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del usuario.", example = "1", required = true)
    },
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Campos que se desean modificar; los omitidos conservan su valor.",
        required = true, useParameterTypeSchema = true,
        content = @Content(mediaType = "application/json", examples = @ExampleObject(
            name = "Ejemplo", value = "{\"nombre\": \"Ana\", \"apellido\": \"Lopez\", \"correo\": \"ana@example.com\", \"telefono\": \"55554444\", \"activo\": true}"))),
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@PutMapping("/{id}")
public ResponseEntity<UsuarioResponseDTO> acutalizarUsuario(
@PathVariable Long id,
        @Valid @RequestBody UsuarioUpdateRequestDTO request){
    Usuario usuarioActualizado=
            usuarioService.actualizar(id, request);

    UsuarioResponseDTO respuesta =
            usuarioMapper.toDTO(usuarioActualizado);

    return ResponseEntity.
            ok(respuesta);

}

// =================================================
// DESACTIVAR USUARIO
// =================================================

@Operation(
    summary = "Desactivar usuario",
    description = "Marca al usuario como inactivo sin borrar sus datos. No permite desactivar al último " +
        "administrador activo. Requiere el permiso DESACTIVAR_USUARIOS.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del usuario.", example = "1", required = true)
    },
    responses = {
        @ApiResponse(responseCode = "204", description = "Operación realizada correctamente, sin contenido de respuesta.", content = @Content),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@DeleteMapping("/{id}")
public ResponseEntity<Void> desactivarUsuario(
        @PathVariable Long id) {

    usuarioService.desactivar(id);

    return ResponseEntity
            .noContent()
            .build();
}

// =================================================
// ASIGNAR ROL A USUARIO
// =================================================

// =================================================
// ASIGNAR ROLES A USUARIO
// =================================================

@Operation(
    summary = "Asignar roles a un usuario",
    description = "Reemplaza los roles del usuario por el conjunto indicado y respeta las restricciones de " +
        "asignación y conservación del último administrador activo. Requiere el permiso ASIGNAR_ROLES.",
    parameters = {
        @Parameter(name = "idUsuario", in = ParameterIn.PATH,
            description = "Identificador del usuario.", example = "1", required = true)
    },
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Conjunto no vacío de identificadores de roles existentes. El nombre del campo es idRol y su " +
            "valor es una lista.",
        required = true, useParameterTypeSchema = true,
        content = @Content(mediaType = "application/json", examples = @ExampleObject(
            name = "Ejemplo", value = "{\"idRol\": [1, 2]}"))),
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@PutMapping("/{idUsuario}/roles")
public ResponseEntity<UsuarioResponseDTO> asignarRoles(
        @PathVariable Long idUsuario,
        @Valid @RequestBody UsuarioRolesRequestDTO request) {

    Usuario usuarioActualizado =
            usuarioService.asignarRol(
                    idUsuario,
                    request.getIdRol()
            );

    UsuarioResponseDTO respuesta =
            usuarioMapper.toDTO(usuarioActualizado);

    return ResponseEntity
            .ok(respuesta);
}

}
