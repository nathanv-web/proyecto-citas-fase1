package com.proyectocitas.config;

import com.proyectocitas.security.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpHeaders;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

    @Test
    void recuperacionEsPublicaPeroNoElRestoDelNamespaceAuth() throws Exception {
        mvc.perform(post("/api/v1/auth/password/forgot")).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/password/reset")).andExpect(status().isNoContent());
        mvc.perform(post("/api/v1/auth/password/otro")).andExpect(status().isUnauthorized());
    }

    @Test
    void estadisticasExigeAccesoAdminYTodosLosPermisosDeConsulta() throws Exception {
        String[] required = {"ACCESO_ADMIN", "VER_USUARIOS", "VER_MEDICOS", "VER_ROLES", "VER_CITAS"};
        mvc.perform(get("/api/v1/admin/statistics")).andExpect(status().isUnauthorized());
        for (String missing : required) {
            var authorities = java.util.Arrays.stream(required)
                    .filter(permission -> !permission.equals(missing))
                    .map(SimpleGrantedAuthority::new).toList();
            mvc.perform(get("/api/v1/admin/statistics").with(user("ana").authorities(authorities)))
                    .andExpect(status().isForbidden());
        }
        mvc.perform(get("/api/v1/admin/statistics").with(user("ana").authorities(
                        java.util.Arrays.stream(required).map(SimpleGrantedAuthority::new).toList())))
                .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "http://localhost:4200", "http://localhost:8100",
        "https://localhost", "http://localhost", "capacitor://localhost"
    })
    void preflightPermiteOrigenesDeAngularEIonicSinJwt(String origin) throws Exception {
        mvc.perform(options("http://localhost:8081/api/v1/appointments")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "authorization, content-type"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"})
    void preflightPermiteLosMetodosDeLaApi(String method) throws Exception {
        mvc.perform(options("/api/v1/especialidades/1/estado")
                .header(HttpHeaders.ORIGIN, "http://localhost:8100")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8100"))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        "GET,POST,PUT,PATCH,DELETE,OPTIONS"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://otro.example", "https://localhost.otro.example", "null"})
    void preflightRechazaOrigenesNoAutorizados(String origin) throws Exception {
        mvc.perform(options("/api/v1/auth/login")
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/api/v1/auth/login", "/api/v1/auth/register"})
    void autenticacionPublicaDesdeIonicNoRequiereJwtNiCsrf(String endpoint) throws Exception {
        mvc.perform(post(endpoint).header(HttpHeaders.ORIGIN, "https://localhost"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://localhost"));
    }

    @Test
    void peticionAutenticadaDesdeIonicConservaCors() throws Exception {
        mvc.perform(patch("/api/v1/especialidades/1/estado")
                .header(HttpHeaders.ORIGIN, "capacitor://localhost")
                .with(user("ana").authorities(new SimpleGrantedAuthority("ACTUALIZAR_ESPECIALIDADES"))))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "capacitor://localhost"));
    }

    @Test
    void erroresDeAutenticacionYPermisosConservanCors() throws Exception {
        mvc.perform(get("/api/v1/admin/statistics").header(HttpHeaders.ORIGIN, "http://localhost:8100"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8100"));
        mvc.perform(get("/api/v1/admin/statistics")
                .header(HttpHeaders.ORIGIN, "http://localhost:8100").with(user("ana")))
                .andExpect(status().isForbidden())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:8100"));
    }

    @Configuration
    @EnableWebMvc
    @EnableWebSecurity
    @Import({SecurityConfig.class, CorsConfig.class})
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
        @PostMapping({"/api/v1/auth/login", "/api/v1/auth/register"})
        ResponseEntity<Void> authentication() { return ResponseEntity.noContent().build(); }
        @PostMapping({"/api/v1/auth/password/forgot", "/api/v1/auth/password/reset"})
        ResponseEntity<Void> recovery() { return ResponseEntity.noContent().build(); }
        @GetMapping("/api/v1/admin/statistics")
        ResponseEntity<Void> statistics() { return ResponseEntity.noContent().build(); }
        @PatchMapping("/api/v1/especialidades/{id}/estado")
        ResponseEntity<Void> patchState() { return ResponseEntity.noContent().build(); }
        @PutMapping({"/api/v1/users/{id}", "/api/v1/medicos/{id}"})
        ResponseEntity<Void> updateState() { return ResponseEntity.noContent().build(); }
        @DeleteMapping({"/api/v1/users/{id}", "/api/v1/medicos/{id}", "/api/v1/especialidades/{id}"})
        ResponseEntity<Void> deactivate() { return ResponseEntity.noContent().build(); }
    }
}
