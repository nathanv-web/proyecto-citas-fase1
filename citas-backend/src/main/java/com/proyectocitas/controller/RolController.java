package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.proyectocitas.dto.response.RolResponseDTO;
import com.proyectocitas.dto.request.RolCreateRequestDTO;
import com.proyectocitas.dto.request.RolUpdateRequestDTO;
import com.proyectocitas.mapper.RolMapper;
import com.proyectocitas.model.Rol;
import com.proyectocitas.service.RolService;

import jakarta.validation.Valid;
import java.util.List;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import com.proyectocitas.dto.request.RolPermisosRequestDTO;


@Tag(name = "Roles", description = "Creación, consulta, actualización y eliminación de roles y asignación de permisos.")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/v1/roles")
public class RolController {

    private final RolService rolService;
    private final RolMapper rolMapper;


    public RolController(
            RolService rolService,
            RolMapper rolMapper) {

        this.rolService = rolService;
        this.rolMapper = rolMapper;
    }


    // =================================================
    // CREAR ROL
    // =================================================

    @Operation(
        summary = "Crear rol",
        description = "Crea un rol con nombre único en formato ROLE_NOMBRE. Requiere el permiso CREAR_ROLES.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Nombre del rol de hasta 50 caracteres y descripción de hasta 255.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"nombre\": \"ROLE_RECEPCION\", \"descripcion\": \"Personal de recepción\"}"))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Registro creado correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping
    public ResponseEntity<RolResponseDTO> crearRol(
            @Valid @RequestBody RolCreateRequestDTO request) {

        Rol rol =
                rolMapper.toEntity(request);

        Rol rolGuardado =
                rolService.guardar(rol);

        RolResponseDTO respuesta =
                rolMapper.toDTO(rolGuardado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }
    // =================================================
// LISTAR, BUSCAR Y PAGINAR ROLES
// =================================================
// Sin parámetros conserva la respuesta anterior en forma
// de lista para no afectar a los consumidores existentes.
//
// Cuando se recibe search, page o size, la respuesta se
// devuelve paginada.
@Operation(
    summary = "Listar roles",
    description = "Sin búsqueda ni paginación devuelve una lista completa. Si search contiene texto no vacío, o se " +
        "proporciona page o size, devuelve una página de resultados; en ese caso page comienza en 0 y " +
        "size utiliza 10 por defecto. Requiere el permiso VER_ROLES.",
    parameters = {
        @Parameter(name = "search", in = ParameterIn.QUERY,
            description = "Texto para buscar roles; una búsqueda no vacía activa la respuesta paginada.", example = "RECEPCION"),
        @Parameter(name = "page", in = ParameterIn.QUERY,
            description = "Número de página desde 0. Si se omite al usar paginación, se utiliza 0.", example = "0", schema = @Schema(type = "integer", format = "int32", minimum = "0")),
        @Parameter(name = "size", in = ParameterIn.QUERY,
            description = "Cantidad de elementos por página, mayor o igual a 1. Si se omite al usar paginación, se utiliza 10.", example = "10", schema = @Schema(type = "integer", format = "int32", minimum = "1"))
    },
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/ListadoRoles"))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@GetMapping
public ResponseEntity<?> listarRoles(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer size) {

    // Mantener compatibilidad con el comportamiento anterior.
    if ((search == null || search.isBlank())
            && page == null
            && size == null) {

        List<RolResponseDTO> roles =
                rolService.obtenerTodos()
                        .stream()
                        .map(rolMapper::toDTO)
                        .toList();

        return ResponseEntity.ok(roles);
    }

    // Valores predeterminados para la paginación.
    int numeroPagina = page != null ? page : 0;
    int tamanoPagina = size != null ? size : 10;

    Pageable pageable =
            PageRequest.of(numeroPagina, tamanoPagina);

    Page<RolResponseDTO> roles =
            rolService.buscarConPaginacion(
                    search,
                    pageable
            ).map(rolMapper::toDTO);

    return ResponseEntity.ok(roles);
}

 // =================================================
// BUSCAR POR ID
// =================================================

@Operation(
    summary = "Consultar rol por ID",
    description = "Obtiene el nombre, la descripción y los permisos de un rol. Requiere el permiso VER_ROLES.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del rol.", example = "1", required = true)
    },
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolResponseDTO.class))),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@GetMapping("/{id}")
public ResponseEntity<RolResponseDTO> buscarRolPorId(
@PathVariable Long id){

    Rol rol =
            rolService.obtenerPorId(id);

    RolResponseDTO respuesta =
            rolMapper.toDTO(rol);

    return ResponseEntity.
            ok(respuesta);

}

// =================================================
// ACTUALIZAR ROL
// =================================================

@Operation(
    summary = "Actualizar rol",
    description = "Actualiza los campos proporcionados del rol; el nombre debe conservar el formato ROLE_NOMBRE. " +
        "Requiere el permiso ACTUALIZAR_ROLES.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del rol.", example = "1", required = true)
    },
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Nombre y descripción que se desean actualizar.",
        required = true, useParameterTypeSchema = true,
        content = @Content(mediaType = "application/json", examples = @ExampleObject(
            name = "Ejemplo", value = "{\"nombre\": \"ROLE_RECEPCION\", \"descripcion\": \"Atención de recepción y coordinación\"}"))),
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolResponseDTO.class))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@PutMapping("/{id}")
public ResponseEntity<RolResponseDTO> actualizarRol(
        @PathVariable Long id,
        @Valid @RequestBody RolUpdateRequestDTO request) {

    Rol rolActualizado =
            rolService.actualizar(request,id );

    RolResponseDTO respuesta =
            rolMapper.toDTO(rolActualizado);

    return ResponseEntity
            .ok(respuesta);
}
// =================================================
// ELIMINAR ROL
// =================================================

@Operation(
    summary = "Eliminar rol",
    description = "Elimina un rol únicamente si no está asignado a ningún usuario. Requiere el permiso " +
        "ELIMINAR_ROLES.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del rol.", example = "1", required = true)
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
public ResponseEntity<Void> eliminarRol(
        @PathVariable Long id) {

    rolService.eliminar(id);

    return ResponseEntity
            .noContent()
            .build();
}

// =================================================
// ASIGNAR PERMISOS A UN ROL
// =================================================

@Operation(
    summary = "Asignar permisos a un rol",
    description = "Reemplaza los permisos del rol por el conjunto indicado. Una lista vacía retira todos los " +
        "permisos del rol. Solo se pueden conceder permisos que el usuario autenticado ya posee. Para " +
        "modificar ROLE_ADMIN se requiere también ACCESO_ADMIN. Requiere el permiso ASIGNAR_PERMISOS.",
    parameters = {
        @Parameter(name = "id", in = ParameterIn.PATH,
            description = "Identificador del rol.", example = "1", required = true)
    },
    requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
        description = "Nombres de permisos válidos del catálogo; el conjunto puede estar vacío.",
        required = true, useParameterTypeSchema = true,
        content = @Content(mediaType = "application/json", examples = @ExampleObject(
            name = "Ejemplo", value = "{\"permisos\": [\"VER_USUARIOS\", \"VER_MEDICOS\"]}"))),
    responses = {
        @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = RolResponseDTO.class))),
        @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
        @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
        @ApiResponse(responseCode = "403", ref = "#/components/responses/Error403"),
        @ApiResponse(responseCode = "404", ref = "#/components/responses/Error404"),
        @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
    }
)
@PutMapping("/{id}/permissions")
public ResponseEntity<RolResponseDTO> asignarPermisos(
        @PathVariable Long id,
        @Valid @RequestBody RolPermisosRequestDTO request) {

    Rol rolActualizado =
            rolService.asignarPermisos(
                    id,
                    request.getPermisos()
            );

    RolResponseDTO respuesta =
            rolMapper.toDTO(
                    rolActualizado
            );

    return ResponseEntity
            .ok(respuesta);
}

}
