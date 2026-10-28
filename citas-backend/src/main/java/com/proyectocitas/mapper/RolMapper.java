package com.proyectocitas.mapper;

import com.proyectocitas.dto.response.RolResponseDTO;
import com.proyectocitas.dto.request.RolCreateRequestDTO;
import com.proyectocitas.model.Rol;

import org.springframework.stereotype.Component;


@Component
public class RolMapper {

    public Rol toEntity(RolCreateRequestDTO dto){
        Rol rol = new Rol();

        rol.setNombre(dto.getNombre());
        rol.setDescripcion(dto.getDescripcion());

        return rol;
    }
public RolResponseDTO toDTO(Rol rol) {

    RolResponseDTO dto = new RolResponseDTO();

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
