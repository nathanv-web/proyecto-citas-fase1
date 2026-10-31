package com.proyectocitas.security;

import com.proyectocitas.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtIdentityPublicTest {
    @Test
    void tokenDeCuentaAnteriorNoAutenticaAlNuevoDueñoDelCorreo() {
        JwtService jwt = new JwtService();
        ReflectionTestUtils.setField(jwt, "secret", "clave-de-prueba-sin-uso-real-12345678901234567890");
        ReflectionTestUtils.setField(jwt, "expiration", 3_600_000L);

        var cuentaAnterior = new Usuario();
        cuentaAnterior.setIdUsuario(1L);
        cuentaAnterior.setCorreo("persona@example.com");
        cuentaAnterior.setActivo(true);
        cuentaAnterior.setVersionSesion(0L);
        String token = jwt.generateToken(cuentaAnterior);

        cuentaAnterior.setCorreo("nuevo@example.com");
        var nuevaCuenta = new Usuario();
        nuevaCuenta.setIdUsuario(2L);
        nuevaCuenta.setCorreo("persona@example.com");
        nuevaCuenta.setActivo(true);
        nuevaCuenta.setVersionSesion(0L);

        assertFalse(jwt.isTokenValid(token, nuevaCuenta));
    }

    @Test
    void unBearerTemporalNoBloqueaElEndpointPublicoDeRecuperacion() throws Exception {
        var jwt = mock(JwtService.class);
        var details = mock(CustomUserDetailsService.class);
        var user = new Usuario();
        user.setDebeCambiarPassword(true);
        when(jwt.extractUsername("temporal")).thenReturn("ana@example.com");
        when(details.loadUserByUsername("ana@example.com")).thenReturn(user);
        when(jwt.isTokenValid("temporal", user)).thenReturn(true);
        var filter = new JwtAuthenticationFilter(jwt, details);
        var request = new MockHttpServletRequest("POST", "/api/v1/auth/password/forgot");
        request.addHeader("Authorization", "Bearer temporal");
        var response = new MockHttpServletResponse();
        try {
            filter.doFilter(request, response, (req, res) -> res.getWriter().write("continuar"));
            assertEquals("continuar", response.getContentAsString());
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
