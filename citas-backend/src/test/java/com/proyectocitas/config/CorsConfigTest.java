package com.proyectocitas.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorsConfigTest {

    @Test
    void origenesConfiguradosReemplazanLosPredeterminadosYAdmitenEspacios() throws Exception {
        try (var context = new AnnotationConfigApplicationContext()) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                    "app.cors.allowed-origins= http://192.168.1.50:8100, , https://citas.example.test, ");
            context.register(CorsConfig.class);
            context.refresh();
            var filter = new CorsFilter(context.getBean(CorsConfigurationSource.class));

            for (String origin : new String[]{"http://192.168.1.50:8100", "https://citas.example.test"}) {
                var response = preflight(filter, origin);
                assertEquals(200, response.getStatus());
                assertEquals(origin, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            }
            for (String origin : new String[]{"http://localhost:8100", "https://otro.example"}) {
                var response = preflight(filter, origin);
                assertEquals(403, response.getStatus());
                assertNull(response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            }
        }
    }

    @Test
    void applicationPropertiesPermiteOrigenesDeAngularEIonic() throws Exception {
        try (var context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getPropertySources()
                    .addLast(new ResourcePropertySource("classpath:application.properties"));
            context.register(CorsConfig.class);
            context.refresh();
            var filter = new CorsFilter(context.getBean(CorsConfigurationSource.class));

            for (String origin : new String[]{"http://localhost:4200", "http://localhost:8100",
                    "https://localhost", "http://localhost", "capacitor://localhost"}) {
                var response = preflight(filter, origin);
                assertEquals(200, response.getStatus(), origin);
                assertEquals(origin, response.getHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
            }
        }
    }

    private MockHttpServletResponse preflight(CorsFilter filter, String origin) throws Exception {
        var request = new MockHttpServletRequest("OPTIONS", "/api/v1/auth/login");
        request.setServerPort(8081);
        request.addHeader(HttpHeaders.ORIGIN, origin);
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");
        request.addHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
