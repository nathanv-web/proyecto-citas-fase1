package com.proyectocitas.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:4300")
public class RolController {

    private final List<Map<String, Object>> permissions = List.of(
        Map.of("id", 1, "name", "VER_USUARIOS", "description", "Permite ver la lista de usuarios"),
        Map.of("id", 2, "name", "VER_ROLES", "description", "Permite consultar roles"),
        Map.of("id", 3, "name", "CREAR_ROLES", "description", "Permite crear nuevos roles"),
        Map.of("id", 4, "name", "ACTUALIZAR_ROLES", "description", "Permite modificar roles"),
        Map.of("id", 5, "name", "ELIMINAR_ROLES", "description", "Permite eliminar roles"),
        Map.of("id", 6, "name", "ASIGNAR_PERMISOS", "description", "Permite asignar permisos a roles"),
        Map.of("id", 7, "name", "REGISTRAR_DIAGNOSTICO", "description", "Permite registrar diagnósticos"),
        Map.of("id", 8, "name", "CREAR_HORARIOS", "description", "Permite gestionar horarios")
    );

    private final List<Map<String, Object>> roles = List.of(
        Map.of(
            "id", 1, 
            "name", "ROLE_JEFE_AREA", 
            "description", "Encargado del área médica",
            "permissions", List.of(
                Map.of("id", 1, "name", "VER_USUARIOS"),
                Map.of("id", 2, "name", "VER_ROLES"),
                Map.of("id", 3, "name", "CREAR_ROLES")
            )
        ),
        Map.of(
            "id", 2, 
            "name", "MEDICO", 
            "description", "Gestión de citas y pacientes",
            "permissions", List.of(
                Map.of("id", 7, "name", "REGISTRAR_DIAGNOSTICO")
            )
        )
    );

    @GetMapping("/roles")
    public List<Map<String, Object>> getRoles() {
        return roles;
    }

    @GetMapping("/roles/{id}")
    public Map<String, Object> getRoleById(@PathVariable Long id) {
        return roles.stream()
            .filter(r -> r.get("id").toString().equals(id.toString()))
            .findFirst()
            .orElse(roles.get(0));
    }

    @PostMapping("/roles")
    public Map<String, Object> createRole(@RequestBody Map<String, Object> role) {
        return role;
    }

    @PutMapping("/roles/{id}")
    public Map<String, Object> updateRole(@PathVariable Long id, @RequestBody Map<String, Object> role) {
        return role;
    }

    @DeleteMapping("/roles/{id}")
    public void deleteRole(@PathVariable Long id) {}

    @GetMapping("/permissions")
    public List<Map<String, Object>> getPermissions() {
        return permissions;
    }

    @PutMapping("/roles/{id}/permissions")
    public void updatePermissions(@PathVariable Long id, @RequestBody List<Long> permissionIds) {}
}