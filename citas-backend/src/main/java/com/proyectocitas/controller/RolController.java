package com.proyectocitas.controller;

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
