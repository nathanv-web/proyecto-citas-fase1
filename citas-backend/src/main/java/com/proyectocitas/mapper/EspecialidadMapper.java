package com.proyectocitas.mapper;

import com.proyectocitas.dto.EspecialidadDTO;
import com.proyectocitas.dto.EspecialidadRequestDTO;
import com.proyectocitas.model.Especialidad;
import org.springframework.stereotype.Component;

@Component
public class EspecialidadMapper {

    public EspecialidadDTO toDTO(
            Especialidad especialidad
    ) {

        if (especialidad == null) {
            return null;
        }

        EspecialidadDTO dto =
                new EspecialidadDTO();

        dto.setIdEspecialidad(
                especialidad.getIdEspecialidad()
        );

        dto.setNombre(
                especialidad.getNombre()
        );

        dto.setDescripcion(
                especialidad.getDescripcion()
        );

        dto.setActivo(
                especialidad.getActivo()
        );

        return dto;
    }


    public Especialidad toEntity(
            EspecialidadRequestDTO dto
    ) {

        if (dto == null) {
            return null;
        }

        Especialidad especialidad =
                new Especialidad();

        especialidad.setNombre(
                dto.getNombre()
        );

        especialidad.setDescripcion(
                dto.getDescripcion()
        );

        if (dto.getActivo() != null) {

            especialidad.setActivo(
                    dto.getActivo()
            );
        }

        return especialidad;
    }
}