package com.proyectocitas.mapper;

import com.proyectocitas.dto.AppointmentRequestDTO;
import com.proyectocitas.dto.AppointmentResponseDTO;
import com.proyectocitas.model.Cita;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    //Convierte una entidad Cita a AppointmentResponseDTO
    public AppointmentResponseDTO toDTO(Cita cita) {

        //Evitar error si la cita viene null
        if (cita == null) {
            return null;
        }

        AppointmentResponseDTO dto = new AppointmentResponseDTO();

        dto.setIdCita(cita.getIdCita());
        dto.setMotivo(cita.getMotivo());
        dto.setDiagnosticoReceta(cita.getDiagnosticoReceta());
        dto.setObservaciones(cita.getObservaciones());


        //Obtener el nombre completo del paciente
        if (cita.getPaciente() != null) {

            String nombrePaciente =
                    cita.getPaciente().getNombre()
                    + " "
                    + cita.getPaciente().getApellido();

            dto.setPaciente(nombrePaciente);
        }


        //Obtener los datos del horario de la cita
        if (cita.getHorario() != null) {

            dto.setFecha(cita.getHorario().getFecha());
            dto.setHoraInicio(cita.getHorario().getHoraInicio());
            dto.setHoraFin(cita.getHorario().getHoraFin());


            //Obtener datos del medico asociado al horario
            if (cita.getHorario().getMedico() != null) {

                //Obtener nombre completo del medico
                if (cita.getHorario().getMedico().getUsuario() != null) {

                    String nombreMedico =
                            cita.getHorario()
                                    .getMedico()
                                    .getUsuario()
                                    .getNombre()
                            + " "
                            + cita.getHorario()
                                    .getMedico()
                                    .getUsuario()
                                    .getApellido();

                    dto.setMedico(nombreMedico);
                }


                //Obtener especialidad del medico
                if (cita.getHorario()
                        .getMedico()
                        .getEspecialidad() != null) {

                    dto.setEspecialidad(
                            cita.getHorario()
                                    .getMedico()
                                    .getEspecialidad()
                                    .getNombre()
                    );
                }
            }
        }


        //Obtener el estado actual de la cita
        if (cita.getEstado() != null
                && cita.getEstado().getNombre() != null) {

            dto.setEstado(
                    cita.getEstado()
                            .getNombre()
                            .name()
            );
        }

        return dto;
    }


    //Convierte AppointmentRequestDTO a una entidad Cita
    public Cita toEntity(AppointmentRequestDTO dto) {

        //Evitar error si el DTO viene null
        if (dto == null) {
            return null;
        }

        Cita cita = new Cita();

        //El motivo es el dato que podemos copiar directamente
        cita.setMotivo(
                dto.getMotivo()
        );

        return cita;
    }
}