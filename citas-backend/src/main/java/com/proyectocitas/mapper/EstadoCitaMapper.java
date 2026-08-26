package com.proyectocitas.mapper;

import com.proyectocitas.dto.EstadoCitaDTO;
import com.proyectocitas.model.EstadoCita;
import org.springframework.stereotype.Component;

@Component
public class EstadoCitaMapper {

    //Convierte una entidad EstadoCita a EstadoCitaDTO
    public EstadoCitaDTO toDTO(EstadoCita estadoCita) {

        //Evitar error si el estado viene null
        if (estadoCita == null) {
            return null;
        }

        EstadoCitaDTO dto = new EstadoCitaDTO();

        dto.setIdEstado(estadoCita.getIdEstado());
        dto.setDescripcion(estadoCita.getDescripcion());

        //Convertir el Enum del estado a texto
        if (estadoCita.getNombre() != null) {
            dto.setNombre(
                    estadoCita.getNombre().name()
            );
        }

        return dto;
    }
}