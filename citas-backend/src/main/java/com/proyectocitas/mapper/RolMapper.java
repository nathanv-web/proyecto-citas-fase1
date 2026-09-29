package com.proyectocitas.mapper;

import com.proyectocitas.dto.RolDTO;
import com.proyectocitas.dto.RolRequestDTO;
import com.proyectocitas.model.Rol;

import org.springframework.stereotype.Component;


@Component
public class RolMapper {
    
    public Rol toEntity(RolRequestDTO dto){
        Rol rol = new Rol();
        
        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());
        
        return rol;
    }
public RolDTO toDTO(Rol rol) {

    RolDTO dto = new RolDTO();

    dto.setIdRol(rol.getIdRol());
    dto.setNombre(rol.getNombre());
    dto.setDescripcion(rol.getDescripcion());

    dto.setPermisos(
            rol.getPermisos()
                    .stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet())
    );

    return dto;
}
}
