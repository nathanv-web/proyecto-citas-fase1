package com.proyectocitas.controller;

import com.proyectocitas.dto.UsuarioDTO;
import com.proyectocitas.mapper.UsuarioMapper;
import com.proyectocitas.model.Rol;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.security.JwtService;
import com.proyectocitas.service.UsuarioService;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthRegisterEndpointTest {

    @Test
    void debeRegistrarPacienteConRolePatientAutomaticamente() throws Exception {

        AuthenticationManager authenticationManager =
                mock(AuthenticationManager.class);

        JwtService jwtService =
                mock(JwtService.class);

        UsuarioService usuarioService =
                mock(UsuarioService.class);

        UsuarioMapper usuarioMapper =
                mock(UsuarioMapper.class);


        AuthController controller =
                new AuthController(
                        authenticationManager,
                        jwtService,
                        usuarioService,
                        usuarioMapper
                );


        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(controller)
                        .build();


        Usuario entidadEntrada = new Usuario();

        entidadEntrada.setNombre("Jaime");
        entidadEntrada.setApellido("Lopez");
        entidadEntrada.setCorreo("jaime@gmail.com");
        entidadEntrada.setTelefono("55555555");
        entidadEntrada.setContraseña("12345678");


        Rol rolPaciente =
                new Rol(
                        "ROLE_PATIENT",
                        "Paciente"
                );


        Usuario usuarioGuardado =
                new Usuario();

        usuarioGuardado.setIdUsuario(1L);
        usuarioGuardado.setNombre("Jaime");
        usuarioGuardado.setApellido("Lopez");
        usuarioGuardado.setCorreo("jaime@gmail.com");
        usuarioGuardado.setTelefono("55555555");
        usuarioGuardado.setActivo(true);

        usuarioGuardado.setRoles(
                Set.of(rolPaciente)
        );


        UsuarioDTO respuesta =
                new UsuarioDTO();

        respuesta.setIdUsuario(1L);
        respuesta.setNombre("Jaime");
        respuesta.setApellido("Lopez");
        respuesta.setCorreo("jaime@gmail.com");
        respuesta.setTelefono("55555555");
        respuesta.setActivo(true);

        respuesta.setRoles(
                Set.of("ROLE_PATIENT")
        );


        when(
                usuarioMapper.toEntity(any())
        )
                .thenReturn(entidadEntrada);


        when(
                usuarioService.registrarPaciente(
                        entidadEntrada
                )
        )
                .thenReturn(usuarioGuardado);


        when(
                usuarioMapper.toDTO(
                        usuarioGuardado
                )
        )
                .thenReturn(respuesta);


        mockMvc.perform(

                post("/api/v1/auth/register")

                        .contentType(
                                MediaType.APPLICATION_JSON
                        )

                        .content("""
                                {
                                  "nombre": "Jaime",
                                  "apellido": "Lopez",
                                  "correo": "jaime@gmail.com",
                                  "telefono": "55555555",
                                  "contrasena": "12345678"
                                }
                                """)
        )

        .andExpect(
                status().isCreated()
        )

        .andExpect(
                jsonPath("$.correo")
                        .value("jaime@gmail.com")
        )

        .andExpect(
                jsonPath("$.roles[0]")
                        .value("ROLE_PATIENT")
        );
    }
}