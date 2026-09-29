package com.proyectocitas.controller;

import com.proyectocitas.dto.RolDTO;
import com.proyectocitas.dto.RolRequestDTO;
import com.proyectocitas.dto.RolUpdateDTO;
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
import com.proyectocitas.dto.RolPermisosRequestDTO;


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
    public ResponseEntity<RolDTO> crearRol(
            @Valid @RequestBody RolRequestDTO request) {

        Rol rol =
                rolMapper.toEntity(request);

        Rol rolGuardado =
                rolService.guardar(rol);

        RolDTO respuesta =
                rolMapper.toDTO(rolGuardado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(respuesta);
    }
    // =================================================
// LISTAR ROLES
// =================================================

@GetMapping
public ResponseEntity<List<RolDTO>> listarRoles() {

    List<RolDTO> roles =
            rolService.obtenerTodos()
                    .stream()
                    .map(rolMapper::toDTO)
                    .toList();

    return ResponseEntity
            .ok(roles);
}

 // =================================================
// BUSCAR POR ID
// =================================================

@GetMapping("/{id}")
public ResponseEntity<RolDTO> buscarRolPorId(
@PathVariable Long id){
    
    Rol rol = 
            rolService.obtenerPorId(id);
    
    RolDTO respuesta = 
            rolMapper.toDTO(rol);
    
    return ResponseEntity.
            ok(respuesta);
    
}

// =================================================
// ACTUALIZAR ROL
// =================================================

@PutMapping("/{id}")
public ResponseEntity<RolDTO> actualizarRol(
        @PathVariable Long id,
        @Valid @RequestBody RolUpdateDTO request) {

    Rol rolActualizado =
            rolService.actualizar(request,id );

    RolDTO respuesta =
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
public ResponseEntity<RolDTO> asignarPermisos(
        @PathVariable Long id,
        @Valid @RequestBody RolPermisosRequestDTO request) {

    Rol rolActualizado =
            rolService.asignarPermisos(
                    id,
                    request.getPermisos()
            );

    RolDTO respuesta =
            rolMapper.toDTO(
                    rolActualizado
            );

    return ResponseEntity
            .ok(respuesta);
}

}