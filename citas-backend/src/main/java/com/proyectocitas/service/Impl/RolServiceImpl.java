package com.proyectocitas.service.Impl;

import com.proyectocitas.dto.RolUpdateDTO;
import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;
import com.proyectocitas.service.RolService;
import com.proyectocitas.repository.UsuarioRepository;
import java.util.HashSet;

import org.springframework.stereotype.Service;



import java.util.List;
import java.util.Optional;
import java.util.Set;
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
    
    @Override
public Rol asignarPermisos(
        Long idRol,
        Set<Permiso> permisos) {

    // Buscar rol existente
    Rol rol = rolRepository.findById(idRol)
            .orElseThrow(() ->
                    new ResourceNotFoundException(
                            "El rol indicado no existe"
                    )
            );

    // Asignar permisos seleccionados
    rol.setPermisos(
            new HashSet<>(permisos)
    );

    // Guardar cambios
    return rolRepository.save(rol);
}
}