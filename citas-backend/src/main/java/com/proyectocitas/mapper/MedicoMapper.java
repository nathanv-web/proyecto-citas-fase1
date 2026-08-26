package com.proyectocitas.mapper;

import com.proyectocitas.dto.MedicoDTO;
import com.proyectocitas.dto.MedicoRequestDTO;
import com.proyectocitas.model.Medico;
import org.springframework.stereotype.Component;

@Component
public class MedicoMapper {

    //Convierte una entidad Medico a MedicoDTO
    public MedicoDTO toDTO(Medico medico) {

        //Evitar error si el medico viene null
        if (medico == null) {
            return null;
        }

        MedicoDTO dto = new MedicoDTO();

        dto.setIdMedico(medico.getIdMedico());
        dto.setColegiado(medico.getColegiado());
        dto.setAniosExperiencia(medico.getAniosExperiencia());
        dto.setBiografia(medico.getBiografia());


        //Convertir el estado Enum a texto
        if (medico.getEstado() != null) {

            dto.setEstado(
                    medico.getEstado().name()
            );
        }


        //Obtener los datos del usuario asociado al medico
        if (medico.getUsuario() != null) {

            String nombreCompleto =
                    medico.getUsuario().getNombre()
                    + " "
                    + medico.getUsuario().getApellido();

            dto.setNombre(nombreCompleto);

            dto.setCorreo(
                    medico.getUsuario().getCorreo()
            );
        }


        //Obtener el nombre de la especialidad
        if (medico.getEspecialidad() != null) {

            dto.setEspecialidad(
                    medico.getEspecialidad().getNombre()
            );
        }


        return dto;
    }


    //Convierte un MedicoRequestDTO a una entidad Medico
    public Medico toEntity(MedicoRequestDTO dto) {

        //Evitar error si el DTO viene null
        if (dto == null) {
            return null;
        }

        Medico medico = new Medico();

        medico.setColegiado(
                dto.getColegiado()
        );

        medico.setAniosExperiencia(
                dto.getAniosExperiencia()
        );

        medico.setBiografia(
                dto.getBiografia()
        );

        return medico;
    }
}