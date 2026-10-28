
package com.proyectocitas.controller;

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
