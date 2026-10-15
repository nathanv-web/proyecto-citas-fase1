package com.proyectocitas.config;

import com.proyectocitas.security.*;
import org.junit.jupiter.api.*;
import org.springframework.context.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EntityStateSecurityTest {
    private AnnotationConfigWebApplicationContext context;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(TestConfig.class);
        context.refresh();
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
    }

    @AfterEach
    void close() { context.close(); }

    @Test
    void reactivarEspecialidadRequiereActualizarNoSoloAccesoOLectura() throws Exception {
        for (String permission : new String[]{"VER_ESPECIALIDADES", "ELIMINAR_ESPECIALIDADES", "ACCESO_ADMIN"}) {
            mvc.perform(patch("/api/v1/especialidades/1/estado")
                    .with(user("ana").authorities(new SimpleGrantedAuthority(permission))))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(patch("/api/v1/especialidades/1/estado")
                .with(user("ana").authorities(new SimpleGrantedAuthority("ACTUALIZAR_ESPECIALIDADES"))))
                .andExpect(status().isNoContent());
    }

    @Test
    void reactivarUsuariosYMedicosConservaSuPermisoDeActualizacion() throws Exception {
        for (String entity : new String[]{"users", "medicos"}) {
            String permission = entity.equals("users") ? "ACTUALIZAR_USUARIOS" : "ACTUALIZAR_MEDICOS";
            mvc.perform(put("/api/v1/" + entity + "/1").with(user("ana")))
                    .andExpect(status().isForbidden());
            mvc.perform(put("/api/v1/" + entity + "/1")
                    .with(user("ana").authorities(new SimpleGrantedAuthority(permission))))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void desactivarRequiereElPermisoCorrespondiente() throws Exception {
        String[][] cases = {{"users", "DESACTIVAR_USUARIOS"}, {"medicos", "ELIMINAR_MEDICOS"}, {"especialidades", "ELIMINAR_ESPECIALIDADES"}};
        for (String[] item : cases) {
            mvc.perform(delete("/api/v1/" + item[0] + "/1").with(user("ana")))
                    .andExpect(status().isForbidden());
            mvc.perform(delete("/api/v1/" + item[0] + "/1")
                    .with(user("ana").authorities(new SimpleGrantedAuthority(item[1]))))
                    .andExpect(status().isNoContent());
        }
    }

    @Test
    void noPermiteReactivarSinSesion() throws Exception {
        mvc.perform(patch("/api/v1/especialidades/1/estado")).andExpect(status().isUnauthorized());
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import(SecurityConfig.class)
    static class TestConfig {
        @Bean CustomUserDetailsService userDetails() { return mock(CustomUserDetailsService.class); }
        @Bean JwtAuthenticationFilter jwtFilter(CustomUserDetailsService details) {
            return new JwtAuthenticationFilter(mock(JwtService.class), details);
        }
        @Bean CustomAccessDeniedHandler deniedHandler() { return new CustomAccessDeniedHandler(); }
        @Bean CustomAuthenticationEntryPoint entryPoint() { return new CustomAuthenticationEntryPoint(); }
        @Bean StatusEndpoints endpoints() { return new StatusEndpoints(); }
    }

    @RestController
    static class StatusEndpoints {
        @PatchMapping("/api/v1/especialidades/{id}/estado")
        ResponseEntity<Void> patchState() { return ResponseEntity.noContent().build(); }
        @PutMapping({"/api/v1/users/{id}", "/api/v1/medicos/{id}"})
        ResponseEntity<Void> updateState() { return ResponseEntity.noContent().build(); }
        @DeleteMapping({"/api/v1/users/{id}", "/api/v1/medicos/{id}", "/api/v1/especialidades/{id}"})
        ResponseEntity<Void> deactivate() { return ResponseEntity.noContent().build(); }
    }
}
