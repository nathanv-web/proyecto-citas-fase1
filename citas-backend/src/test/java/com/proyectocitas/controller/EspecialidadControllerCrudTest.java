package com.proyectocitas.controller;

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
    void debeActualizarEspecialidad() throws Exception {

        EspecialidadService service =
                mock(EspecialidadService.class);

        EspecialidadController controller =
                new EspecialidadController(service);

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();

        Especialidad actualizada =
                new Especialidad(
                        "Cardiología",
                        "Especialidad del corazón"
                );

        actualizada.setIdEspecialidad(1L);
        actualizada.setActivo(true);

        when(
                service.actualizar(
                        any(Especialidad.class),
                        eq(1L)
                )
        ).thenReturn(actualizada);

        mockMvc.perform(
                put("/api/v1/especialidades/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "nombre": "Cardiología",
                              "descripcion": "Especialidad del corazón",
                              "activo": true
                            }
                        """)
        )
        .andExpect(status().isOk())
        .andExpect(
                jsonPath("$.idEspecialidad").value(1)
        )
        .andExpect(
                jsonPath("$.nombre").value("Cardiología")
        );
    }
}