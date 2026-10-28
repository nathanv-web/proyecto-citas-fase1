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
                        jsonPath("$[?(@ == 'ACCESO_ADMIN')]").exists()
                )
                .andExpect(
                        jsonPath("$[?(@ == 'ACCESO_MEDICO')]").exists()
                )
                .andExpect(
                        jsonPath("$[?(@ == 'ACCESO_PACIENTE')]").exists()
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

    @Test
void debeListarMetadataDePermisosSinCambiarNombres() throws Exception {

    PermisoController controller =
            new PermisoController();

    MockMvc mockMvc =
            MockMvcBuilders
                    .standaloneSetup(controller)
                    .build();

    mockMvc.perform(
            get("/api/v1/permissions/metadata")
    )
            .andExpect(
                    status().isOk()
            )
            .andExpect(
                    jsonPath("$").isArray()
            )
            .andExpect(
                    jsonPath(
                            "$[?(@.nombre == 'VER_USUARIOS' && @.categoria == 'USUARIOS')]"
                    ).exists()
            )
            .andExpect(
                    jsonPath(
                            "$[?(@.nombre == 'ASIGNAR_PERMISOS' && @.categoria == 'ROLES_Y_PERMISOS')]"
                    ).exists()
            )
            .andExpect(
                    jsonPath(
                            "$[?(@.nombre == 'ACCESO_ADMIN' && @.categoria == 'ACCESO')]"
                    ).exists()
            );
}
}
