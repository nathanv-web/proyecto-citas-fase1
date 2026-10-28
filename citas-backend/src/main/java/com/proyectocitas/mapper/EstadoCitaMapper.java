package com.proyectocitas.mapper;

import com.proyectocitas.dto.response.EstadoCitaResponseDTO;
import com.proyectocitas.model.EstadoCita;
import org.springframework.stereotype.Component;

@Component
public class EstadoCitaMapper {

    //Convierte una entidad EstadoCita a EstadoCitaResponseDTO
    public EstadoCitaResponseDTO toDTO(EstadoCita estadoCita) {

        //Evitar error si el estado viene null
        if (estadoCita == null) {
            return null;
        }

        EstadoCitaResponseDTO dto = new EstadoCitaResponseDTO();

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
