package com.proyectocitas.controller;

import com.proyectocitas.dto.RolDTO;
import com.proyectocitas.mapper.RolMapper;
import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.service.RolService;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RolPermisosControllerTest {

    @Test
    void debeAsignarPermisosAUnRol() throws Exception {

        RolService rolService =
                mock(RolService.class);

        RolMapper rolMapper =
                mock(RolMapper.class);


        RolController controller =
                new RolController(
                        rolService,
                        rolMapper
                );


        // =================================================
        // ROL ACTUALIZADO
        // =================================================

        Rol rol =
                new Rol(
                        "ROLE_SECRETARY",
                        "Personal administrativo"
                );

        rol.setIdRol(4L);

        rol.setPermisos(
                Set.of(
                        Permiso.VER_MEDICOS,
                        Permiso.VER_CITAS,
                        Permiso.CREAR_CITA
                )
        );


        // =================================================
        // DTO DE RESPUESTA
        // =================================================

        RolDTO rolDTO =
                new RolDTO();

        rolDTO.setIdRol(4L);
        rolDTO.setNombre("ROLE_SECRETARY");
        rolDTO.setDescripcion(
                "Personal administrativo"
        );

        rolDTO.setPermisos(
                Set.of(
                        "VER_MEDICOS",
                        "VER_CITAS",
                        "CREAR_CITA"
                )
        );


        // =================================================
        // MOCK DEL SERVICIO
        // =================================================

        when(
                rolService.asignarPermisos(
                        4L,
                        Set.of(
                                Permiso.VER_MEDICOS,
                                Permiso.VER_CITAS,
                                Permiso.CREAR_CITA
                        )
                )
        )
                .thenReturn(rol);


        // =================================================
        // MOCK DEL MAPPER
        // =================================================

        when(
                rolMapper.toDTO(rol)
        )
                .thenReturn(rolDTO);


        // =================================================
        // MOCK MVC
        // =================================================

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();


        // =================================================
        // PETICIÓN
        // =================================================

        mockMvc.perform(
                put("/api/v1/roles/4/permissions")

                        .contentType(
                                MediaType.APPLICATION_JSON
                        )

                        .content("""
                                {
                                  "permisos": [
                                    "VER_MEDICOS",
                                    "VER_CITAS",
                                    "CREAR_CITA"
                                  ]
                                }
                                """)
        )

                // =================================================
                // VALIDACIONES
                // =================================================

                .andExpect(
                        status().isOk()
                )

                .andExpect(
                        jsonPath("$.idRol")
                                .value(4)
                )

                .andExpect(
                        jsonPath("$.nombre")
                                .value("ROLE_SECRETARY")
                )

                .andExpect(
                        jsonPath("$.descripcion")
                                .value("Personal administrativo")
                )

                .andExpect(
                        jsonPath("$.permisos")
                                .isArray()
                )

                .andExpect(
                        jsonPath(
                                "$.permisos[?(@ == 'VER_MEDICOS')]"
                        )
                                .exists()
                )

                .andExpect(
                        jsonPath(
                                "$.permisos[?(@ == 'VER_CITAS')]"
                        )
                                .exists()
                )

                .andExpect(
                        jsonPath(
                                "$.permisos[?(@ == 'CREAR_CITA')]"
                        )
                                .exists()
                );
    }
}