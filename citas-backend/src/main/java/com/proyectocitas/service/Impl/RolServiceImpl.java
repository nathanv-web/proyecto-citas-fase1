package com.proyectocitas.service.Impl;

import com.proyectocitas.dto.RolUpdateDTO;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.service.RolService;
import com.proyectocitas.service.AuditoriaService;
import com.proyectocitas.repository.UsuarioRepository;
import java.util.HashSet;

import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class RolServiceImpl implements RolService {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;

    //Constructor para inyectar el repositorio

    public RolServiceImpl(RolRepository rolRepository,
            UsuarioRepository usuarioRepository){
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
    }

    //Obtener todos los roles

    @Override
    public List<Rol> obtenerTodos() {
        return rolRepository.findAll();
    }

    // =================================================
    // BUSCAR ROLES CON PAGINACIÓN
    // =================================================
    // Si no se recibe texto de búsqueda, devuelve todos
    // los roles paginados. Si se recibe search, realiza
    // una búsqueda parcial ignorando mayúsculas/minúsculas.
    @Override
    public Page<Rol> buscarConPaginacion(
        String search,
        Pageable pageable) {

    if (search == null || search.isBlank()) {
        return rolRepository.findAll(pageable);
    }

    return rolRepository.findByNombreContainingIgnoreCase(
            search.trim(),
            pageable
    );
}

    //Buscar un rol por su ID

    @Override
    public Rol obtenerPorId(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() ->
                new ResourceNotFoundException(" El rol indicado no existe"));
                
    }

    //Buscar un rol por su nombre

    @Override
    public Optional<Rol> obtenerPorNombre(String nombre) {
        return rolRepository.findByNombre(nombre);
    }

    //Guardar un nuevo rol

    @Override
    public Rol guardar(Rol rol) {
        if (rolRepository.existsByNombre(rol.getNombre())) {
            throw new DuplicateResourceException(
            "Ya existe un rol con este nombre");
        }
        return rolRepository.save(rol);
    }

    //Actualizar solamente los campos que tengan información

    @Override
    public Rol actualizar(
           RolUpdateDTO rol
            , Long id) {
        
        Rol rolDB = rolRepository.findById(id)
                .orElseThrow(() -> 
                        new  ResourceNotFoundException("El rol indicado no existe"));
        
        if (rol.getNombre() != null) {
            if (!rol.getNombre()
                .equalsIgnoreCase(rolDB.getNombre())){
            if (rolRepository.existsByNombre(rol.getNombre())) {
                throw new DuplicateResourceException(
                "Ya existe un rol con este nombre");
            }
            }
                rolDB.setNombre(rol.getNombre());
        }
        
        if (rol.getDescripcion() != null) {
            rolDB.setDescripcion(rol.getDescripcion());
        }
        return rolRepository.save(rolDB);
        
    }

    //Eliminar un rol y la verificacion si esta asignado a uno o mas usuarios 

    @Override
    public void eliminar(Long id) {
        
        Rol rol = rolRepository.findById(id)
                .orElseThrow(()->
                new ResourceNotFoundException("El rol indicado no existe"));
        
        if (usuarioRepository.existsByRoles_IdRol(id)) {
            throw new DuplicateResourceException("No se puede eliminar el rol porque esta asignado a uno o más usuarios");
                    }
        rolRepository.delete(rol);
    }
    
    // =================================================
// ASIGNAR PERMISOS A UN ROL
// =================================================
// Protecciones:
// - El usuario debe estar autenticado.
// - Solo puede conceder permisos que él mismo posee.
// - ROLE_ADMIN solo puede ser modificado por alguien
//   que tenga ACCESO_ADMIN.
@Override
public Rol asignarPermisos(
        Long idRol,
        Set<Permiso> permisos) {

    Authentication authentication =
            SecurityContextHolder
                    .getContext()
                    .getAuthentication();

    if (authentication == null
            || !authentication.isAuthenticated()) {

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "No fue posible identificar al usuario autenticado."
        );
    }

    Rol rol =
            rolRepository.findById(idRol)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "El rol indicado no existe"
                            )
                    );

    // Solo un usuario con acceso administrativo puede
    // modificar los permisos de ROLE_ADMIN.
    if ("ROLE_ADMIN".equalsIgnoreCase(
            rol.getNombre()
    )) {

        boolean tieneAccesoAdmin =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                Permiso.ACCESO_ADMIN
                                        .name()
                                        .equals(
                                                authority.getAuthority()
                                        )
                        );

        if (!tieneAccesoAdmin) {

    AuditoriaService.registrar(
            "ASIGNAR_PERMISOS",
            "ROL_ID_" + idRol,
            "FALLO",
            "Intento no autorizado de modificar permisos de ROLE_ADMIN"
    );

    throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No tienes autorización para modificar los permisos del rol administrador."
    );
}
    }

    // El actor solamente puede conceder permisos
    // que ya posee personalmente.
    for (Permiso permiso : permisos) {

        boolean poseePermiso =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                permiso.name()
                                        .equals(
                                                authority.getAuthority()
                                        )
                        );

        if (!poseePermiso) {

    AuditoriaService.registrar(
            "ASIGNAR_PERMISOS",
            "ROL_ID_" + idRol,
            "FALLO",
            "Intento no autorizado de asignar el permiso "
                    + permiso.name()
    );

    throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "No tienes autorización para asignar el permiso "
                    + permiso.name()
                    + "."
    );
}
    }

    // Solo después de pasar todas las validaciones
    // se modifica el rol.
    rol.setPermisos(
        new HashSet<>(permisos)
);

Rol rolActualizado =
        rolRepository.save(rol);

AuditoriaService.registrar(
        "ASIGNAR_PERMISOS",
        "ROL_ID_" + idRol,
        "EXITO",
        "Permisos actualizados correctamente"
);

return rolActualizado;
}
}