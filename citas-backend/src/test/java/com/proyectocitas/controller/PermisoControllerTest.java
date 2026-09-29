package com.proyectocitas.controller;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PermisoControllerTest {

    @Test
    void debeListarPermisosDisponibles() throws Exception {

        PermisoController controller =
                new PermisoController();

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();


        mockMvc.perform(
                get("/api/v1/permissions")
        )
                .andExpect(
                        status().isOk()
                )
                .andExpect(
                        jsonPath("$").isArray()
                )
                .andExpect(
                        jsonPath("$[?(@ == 'CREAR_USUARIOS')]")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[?(@ == 'CREAR_HORARIOS')]")
                                .exists()
                )
                .andExpect(
                        jsonPath("$[?(@ == 'REGISTRAR_DIAGNOSTICO')]")
                                .exists()
                );
    }
}