package com.proyectocitas.mapper;

import com.proyectocitas.dto.HorarioDisponibleDTO;
import com.proyectocitas.dto.HorarioDisponibleRequestDTO;
import com.proyectocitas.model.HorarioDisponible;
import org.springframework.stereotype.Component;

@Component
public class HorarioDisponibleMapper {

    //Convierte una entidad HorarioDisponible a HorarioDisponibleDTO
    public HorarioDisponibleDTO toDTO(HorarioDisponible horario) {

        //Evitar error si el horario viene null
        if (horario == null) {
            return null;
        }

        HorarioDisponibleDTO dto = new HorarioDisponibleDTO();

        dto.setIdHorario(horario.getIdHorario());
        dto.setFecha(horario.getFecha());
        dto.setHoraInicio(horario.getHoraInicio());
        dto.setHoraFin(horario.getHoraFin());

        //Convertir el estado Enum a texto
        if (horario.getEstado() != null) {
            dto.setEstado(horario.getEstado().name());
        }

        //Obtener datos del medico relacionado con el horario
        if (horario.getMedico()!= null) {

            dto.setIdMedico(
                    horario.getMedico().getIdMedico()
            );

            //Obtener nombre completo del medico
            if (horario.getMedico().getUsuario() != null) {

                String nombreCompleto =
                        horario.getMedico().getUsuario().getNombre()
                        + " "
                        + horario.getMedico().getUsuario().getApellido();

                dto.setMedico(nombreCompleto);
            }
        }

        return dto;
    }


    //Convierte un HorarioDisponibleRequestDTO a entidad HorarioDisponible
    public HorarioDisponible toEntity(HorarioDisponibleRequestDTO dto) {

        //Evitar error si el DTO viene null
        if (dto == null) {
            return null;
        }

        HorarioDisponible horario = new HorarioDisponible();

        horario.setFecha(dto.getFecha());
        horario.setHoraInicio(dto.getHoraInicio());
        horario.setHoraFin(dto.getHoraFin());

        return horario;
    }
}