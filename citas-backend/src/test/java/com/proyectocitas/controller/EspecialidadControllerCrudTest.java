package com.proyectocitas.controller;

import com.proyectocitas.dto.EspecialidadDTO;
import com.proyectocitas.dto.EspecialidadRequestDTO;
import com.proyectocitas.mapper.EspecialidadMapper;
import com.proyectocitas.model.Especialidad;
import com.proyectocitas.service.EspecialidadService;

import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class EspecialidadControllerCrudTest {


    @Test
    void debeActualizarEspecialidad()
            throws Exception {


        // =============================================
        // MOCK SERVICE
        // =============================================

        EspecialidadService service =
                mock(EspecialidadService.class);


        // =============================================
        // MOCK MAPPER
        // =============================================

        EspecialidadMapper mapper =
                mock(EspecialidadMapper.class);


        // =============================================
        // CONTROLLER
        // =============================================

        EspecialidadController controller =
                new EspecialidadController(
                        service,
                        mapper
                );


        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();


        // =============================================
        // ENTIDAD
        // =============================================

        Especialidad especialidad =
                new Especialidad(
                        "Cardiología",
                        "Especialidad del corazón"
                );


        // =============================================
        // ENTIDAD ACTUALIZADA
        // =============================================

        Especialidad actualizada =
                new Especialidad(
                        "Cardiología",
                        "Especialidad del corazón"
                );

        actualizada.setIdEspecialidad(1L);
        actualizada.setActivo(true);


        // =============================================
        // DTO RESPUESTA
        // =============================================

        EspecialidadDTO respuesta =
                new EspecialidadDTO();

        respuesta.setIdEspecialidad(1L);
        respuesta.setNombre(
                "Cardiología"
        );

        respuesta.setDescripcion(
                "Especialidad del corazón"
        );

        respuesta.setActivo(true);


        // =============================================
        // COMPORTAMIENTO DEL MAPPER
        // =============================================

        when(
                mapper.toEntity(
                        any(EspecialidadRequestDTO.class)
                )
        ).thenReturn(especialidad);


        when(
                mapper.toDTO(actualizada)
        ).thenReturn(respuesta);


        // =============================================
        // COMPORTAMIENTO DEL SERVICE
        // =============================================

        when(
                service.actualizar(
                        any(Especialidad.class),
                        eq(1L)
                )
        ).thenReturn(actualizada);


        // =============================================
        // PETICIÓN PUT
        // =============================================

        mockMvc.perform(

                put(
                        "/api/v1/especialidades/1"
                )

                .contentType(
                        MediaType.APPLICATION_JSON
                )

                .content("""
                        {
                          "nombre": "Cardiología",
                          "descripcion": "Especialidad del corazón",
                          "activo": true
                        }
                        """)
        )


        // =============================================
        // VALIDACIONES
        // =============================================

        .andExpect(
                status().isOk()
        )

        .andExpect(
                jsonPath(
                        "$.idEspecialidad"
                ).value(1)
        )

        .andExpect(
                jsonPath(
                        "$.nombre"
                ).value("Cardiología")
        )

        .andExpect(
                jsonPath(
                        "$.descripcion"
                ).value(
                        "Especialidad del corazón"
                )
        )

        .andExpect(
                jsonPath(
                        "$.activo"
                ).value(true)
        );
    }
}