
package com.proyectocitas.controller;

import com.proyectocitas.dto.UsuarioDTO;
import com.proyectocitas.dto.UsuarioRequestDTO;
import com.proyectocitas.dto.UsuarioRolesRequestDTO;
import com.proyectocitas.dto.UsuarioUpdateDTO;
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
    public ResponseEntity<UsuarioDTO> crearUsuario(
            @Valid @RequestBody UsuarioRequestDTO request) {

        Usuario usuario =
                usuarioMapper.toEntity(request);

        Usuario usuarioGuardado =
                usuarioService.guardar(usuario);

        UsuarioDTO respuesta =
                usuarioMapper.toDTO(usuarioGuardado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }
    // =================================================
// LISTAR USUARIOS
// =================================================

@GetMapping
public ResponseEntity<List<UsuarioDTO>> listarUsuarios() {

    List<UsuarioDTO> usuarios =
            usuarioService.obtenerTodos()
                    .stream()
                    .map(usuarioMapper::toDTO)
                    .toList();

    return ResponseEntity
            .ok(usuarios);
   }
// =================================================
// BUSCAR USUARIO POR ID
// =================================================

@GetMapping("/{id}")
public ResponseEntity<UsuarioDTO> buscarUsuarioPorId(
        @PathVariable Long id) {

    Usuario usuario =
            usuarioService.obtenerPorId(id);

    UsuarioDTO respuesta =
            usuarioMapper.toDTO(usuario);

    return ResponseEntity
            .ok(respuesta);
}
// =================================================
// Actualizar datos
// =================================================
@PutMapping("/{id}")
public ResponseEntity<UsuarioDTO> acutalizarUsuario(
@PathVariable Long id,
        @Valid @RequestBody UsuarioUpdateDTO request){
    Usuario usuarioActualizado= 
            usuarioService.actualizar(id, request);
    
    UsuarioDTO respuesta = 
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
public ResponseEntity<UsuarioDTO> asignarRoles(
        @PathVariable Long idUsuario,
        @Valid @RequestBody UsuarioRolesRequestDTO request) {

    Usuario usuarioActualizado =
            usuarioService.asignarRol(
                    idUsuario,
                    request.getIdRol()
            );

    UsuarioDTO respuesta =
            usuarioMapper.toDTO(usuarioActualizado);

    return ResponseEntity
            .ok(respuesta);
}

}
