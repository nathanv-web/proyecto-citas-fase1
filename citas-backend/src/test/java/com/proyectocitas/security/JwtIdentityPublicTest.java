package com.proyectocitas.security;

import com.proyectocitas.model.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtIdentityPublicTest {
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
