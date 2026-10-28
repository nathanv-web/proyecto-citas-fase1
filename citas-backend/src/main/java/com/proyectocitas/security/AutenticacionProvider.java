package com.proyectocitas.security;

import com.proyectocitas.model.Usuario;
import com.proyectocitas.service.AutenticacionService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Conserva el AuthenticationManager existente y centraliza el bloqueo persistente. */
@Component
public class AutenticacionProvider implements AuthenticationProvider {
    private final AutenticacionService service;

    public AutenticacionProvider(AutenticacionService service) {
        this.service = service;
    }

    @Override
    public Authentication authenticate(Authentication authentication) {
        Usuario usuario = service.autenticar(authentication.getName(),
                authentication.getCredentials() == null ? null : authentication.getCredentials().toString());
        return UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
