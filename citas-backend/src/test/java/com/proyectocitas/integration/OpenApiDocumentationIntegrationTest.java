package com.proyectocitas.integration;

import com.jayway.jsonpath.JsonPath;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:openapi-documentation;MODE=MariaDB;DB_CLOSE_DELAY=-1",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa", "spring.datasource.password=",
    "spring.jpa.hibernate.ddl-auto=create-drop", "spring.jpa.show-sql=false",
    "spring.sql.init.mode=never",
    "jwt.secret=solo-pruebas-openapi-sin-valor-productivo-12345678901234567890"
})
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("unchecked")
class OpenApiDocumentationIntegrationTest {
    private static final Set<String> HTTP_METHODS = Set.of("get", "post", "put", "patch", "delete");
    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login", "/api/v1/auth/register",
            "/api/v1/auth/password/forgot", "/api/v1/auth/password/reset",
            "/api/v1/auth/email/request", "/api/v1/auth/email/verify");

    @Autowired MockMvc mvc;
    @Autowired @Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mappings;
    @Autowired tools.jackson.databind.ObjectMapper json;
    @Autowired jakarta.validation.Validator validator;
    @Autowired com.proyectocitas.repository.RolRepository roles;
    private Map<String, Object> document;

    @BeforeAll
    void cargarDocumentoGenerado() throws Exception {
        String json = mvc.perform(get("/v3/api-docs")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        document = JsonPath.parse(json).read("$");
        Files.writeString(Path.of("target/openapi.json"), json);
    }

    @Test
    void cadaEndpointRegistradoTieneTituloDescripcionGrupoYParametrosExplicados() {
        Set<String> registered = new HashSet<>();
        mappings.getHandlerMethods().forEach((mapping, handler) -> {
            if (handler.getBeanType().getPackageName().equals("com.proyectocitas.controller")) {
                mapping.getPatternValues().forEach(path -> mapping.getMethodsCondition().getMethods()
                        .forEach(method -> registered.add(method.name().toLowerCase() + " " + path)));
            }
        });
        Set<String> documented = new HashSet<>();
        map(document.get("paths")).forEach((path, item) -> map(item).forEach((method, value) -> {
            if (!HTTP_METHODS.contains(method)) return;
            String label = method + " " + path;
            documented.add(label);
            Map<String, Object> operation = map(value);
            map(operation.get("responses")).forEach((code, response) -> {
                if (!code.startsWith("2") || code.equals("204")) return;
                map(map(response).get("content")).forEach((type, media) ->
                        assertFalse(map(map(media).get("schema")).isEmpty(),
                                "Respuesta sin esquema: " + path + " " + code + " " + type));
            });
            assertFalse(string(operation.get("summary")).isBlank(), "Falta título: " + label);
            assertFalse(string(operation.get("description")).isBlank(), "Falta descripción: " + label);
            List<String> tags = (List<String>) operation.get("tags");
            assertNotNull(tags, label);
            assertEquals(1, tags.size(), label);
            assertFalse(tags.getFirst().endsWith("controller"), "Grupo automático: " + label);
            for (Object parameter : (List<?>) operation.getOrDefault("parameters", List.of())) {
                Map<String, Object> data = map(parameter);
                assertFalse(string(data.get("description")).isBlank(), "Parámetro sin explicación: " + label);
                assertFalse(Set.of("principal", "usuario", "http").contains(data.get("name")), label);
            }
        }));
        assertEquals(52, registered.size());
        assertEquals(registered, documented);
        assertEquals(15, ((List<?>) document.get("tags")).size());
    }

    @Test
    void elDocumentoDistingueLasSeisOperacionesPublicasYLosRecursosConJwt() {
        map(document.get("paths")).forEach((path, item) -> map(item).forEach((method, value) -> {
            if (!HTTP_METHODS.contains(method)) return;
            List<?> security = (List<?>) map(value).getOrDefault("security", List.of());
            if (PUBLIC_PATHS.contains(path)) assertTrue(security.isEmpty(), path);
            else assertTrue(security.stream().map(OpenApiDocumentationIntegrationTest::map)
                    .anyMatch(requirement -> requirement.containsKey("bearerAuth")), path);
        }));
        assertEquals("bearer", map(map(map(document.get("components")).get("securitySchemes"))
                .get("bearerAuth")).get("scheme"));
    }

    @Test
    void losEjemplosDeEntradaMantienenElEsquemaYLosErroresNoUsanElDtoDeExito() {
        map(document.get("paths")).forEach((path, item) -> map(item).forEach((method, value) -> {
            if (!HTTP_METHODS.contains(method)) return;
            Map<String, Object> operation = map(value);
            if (operation.containsKey("requestBody") && !path.equals("/api/v1/me/photo")) {
                Map<String, Object> json = map(map(map(operation.get("requestBody")).get("content"))
                        .get("application/json"));
                assertTrue(json.containsKey("schema"), path);
                assertTrue(json.containsKey("examples"), "Falta ejemplo JSON: " + path);
            }
            map(operation.get("responses")).forEach((code, response) -> {
                if (!code.startsWith("4") && !code.equals("500")) return;
                Map<String, Object> error = resolve(map(response));
                Map<String, Object> schema = map(map(map(error.get("content"))
                        .get("application/json")).get("schema"));
                assertEquals("#/components/schemas/RespuestaError", schema.get("$ref"), path + " " + code);
            });
        }));
        assertEquals("#/components/schemas/AppointmentResponseDTO",
                successSchema("/api/v1/appointments", "post", "201").get("$ref"));
        assertEquals("array", successSchema("/api/v1/appointments/my-history", "get", "200").get("type"));
        assertFalse(Boolean.TRUE.equals(map(operation("/api/v1/appointments/{id}/cancel", "put")
                .get("requestBody")).get("required")));
    }

    @Test
    void laFotoSeDescribeComoArchivoYLasRespuestas204NoTienenCuerpo() {
        Map<String, Object> upload = operation("/api/v1/me/photo", "put");
        Map<String, Object> multipart = map(map(map(upload.get("requestBody")).get("content"))
                .get("multipart/form-data"));
        Map<String, Object> schema = resolve(map(multipart.get("schema")));
        Map<String, Object> photo = map(map(schema.get("properties")).get("foto"));
        assertEquals("binary", photo.get("format"));
        Map<String, Object> responses = map(operation("/api/v1/me/photo", "get").get("responses"));
        Map<String, Object> imageTypes = map(map(responses.get("200")).get("content"));
        assertTrue(imageTypes.keySet().containsAll(Set.of("image/jpeg", "image/png")));
        assertTrue(responses.containsKey("204"));
        map(document.get("paths")).forEach((path, item) -> map(item).forEach((method, value) -> {
            if (!HTTP_METHODS.contains(method)) return;
            Map<String, Object> response = map(map(value).get("responses"));
            if (response.containsKey("204")) assertFalse(map(response.get("204")).containsKey("content"), path);
        }));
    }

    @Test
    void rolesYPerfilDescribenSusEnvolturasRealesYSwaggerUiEstaDisponible() throws Exception {
        Map<String, Object> roles = resolve(successSchema("/api/v1/roles", "get", "200"));
        assertTrue(roles.containsKey("oneOf"), "La lista de roles debe describir los dos formatos de respuesta");
        assertEquals(2, ((List<?>) roles.get("oneOf")).size());
        Map<String, Object> profile = resolve(successSchema("/api/v1/me", "get", "200"));
        assertTrue(map(profile.get("properties")).keySet().containsAll(Set.of("usuario", "debeCambiarPassword")));
        mvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }

    @Test
    void losEjemplosJsonSeDeserializanYRespetanLasValidacionesDeLosDtos() {
        mappings.getHandlerMethods().forEach((mapping, handler) -> {
            if (!handler.getBeanType().getPackageName().equals("com.proyectocitas.controller")) return;
            for (var parameter : handler.getMethodParameters()) {
                if (!parameter.hasParameterAnnotation(org.springframework.web.bind.annotation.RequestBody.class)) continue;
                mapping.getPatternValues().forEach(path -> mapping.getMethodsCondition().getMethods().forEach(method -> {
                    Map<String, Object> request = map(operation(path, method.name().toLowerCase()).get("requestBody"));
                    Map<String, Object> media = map(map(request.get("content")).get("application/json"));
                    map(media.get("examples")).forEach((name, example) -> {
                        Object dto = json.convertValue(map(example).get("value"), parameter.getParameterType());
                        assertTrue(validator.validate(dto).isEmpty(), "Ejemplo inválido para " + path);
                    });
                }));
            }
        });
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(authorities = "VER_ROLES")
    void laPaginacionInvalidaDevuelveEl400AnunciadoEnSwagger() throws Exception {
        mvc.perform(get("/api/v1/roles").param("page", "-1").param("size", "10"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/roles").param("page", "0").param("size", "0"))
                .andExpect(status().isBadRequest());
        assertTrue(map(operation("/api/v1/roles", "get").get("responses")).containsKey("400"));
        for (Object value : (List<?>) operation("/api/v1/roles", "get").get("parameters")) {
            Map<String, Object> parameter = map(value);
            if (Set.of("page", "size").contains(parameter.get("name"))) {
                assertEquals("integer", map(parameter.get("schema")).get("type"));
            }
        }
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(authorities = "ASIGNAR_PERMISOS")
    void swaggerExplicaLasRestriccionesQueProducen403AlAsignarPermisos() throws Exception {
        var administrator = roles.findByNombre("ROLE_ADMIN").orElseThrow();
        mvc.perform(put("/api/v1/roles/{id}/permissions", administrator.getIdRol())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"permisos\":[]}"))
                .andExpect(status().isForbidden());
        var patient = roles.findByNombre("ROLE_PATIENT").orElseThrow();
        mvc.perform(put("/api/v1/roles/{id}/permissions", patient.getIdRol())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"permisos\":[\"VER_MEDICOS\"]}"))
                .andExpect(status().isForbidden());
        String description = string(operation("/api/v1/roles/{id}/permissions", "put").get("description"));
        assertTrue(description.contains("ACCESO_ADMIN"));
        assertTrue(description.contains("posee"));
    }

    private Map<String, Object> successSchema(String path, String method, String code) {
        Map<String, Object> response = resolve(map(map(operation(path, method).get("responses")).get(code)));
        return map(map(map(response.get("content")).get("application/json")).get("schema"));
    }

    private Map<String, Object> operation(String path, String method) {
        return map(map(map(document.get("paths")).get(path)).get(method));
    }

    private Map<String, Object> resolve(Map<String, Object> value) {
        if (!value.containsKey("$ref")) return value;
        String[] parts = string(value.get("$ref")).substring(2).split("/");
        Map<String, Object> result = document;
        for (String part : parts) result = map(result.get(part));
        return result;
    }

    private static Map<String, Object> map(Object value) {
        return value == null ? Map.of() : (Map<String, Object>) value;
    }

    private static String string(Object value) { return value == null ? "" : value.toString(); }
}
