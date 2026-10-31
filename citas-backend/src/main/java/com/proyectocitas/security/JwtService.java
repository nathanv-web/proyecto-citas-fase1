package com.proyectocitas.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    public String generateToken(UserDetails userDetails) {
        com.proyectocitas.model.Usuario usuario =
                (com.proyectocitas.model.Usuario) userDetails;
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("idUsuario", usuario.getIdUsuario())
                .claim("versionSesion", usuario.getVersionSesion())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey()).compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);

        return username.equals(userDetails.getUsername())
                && !isTokenExpired(token)
                && userDetails.isEnabled() && userDetails.isAccountNonLocked()
                && versionValida(token, userDetails)
                && usuarioValido(token, userDetails);
    }

    private boolean usuarioValido(String token, UserDetails userDetails) {
        Number idToken = extractClaim(token, claims -> (Number) claims.get("idUsuario"));
        Long idActual = ((com.proyectocitas.model.Usuario) userDetails).getIdUsuario();
        return idToken != null && idActual != null && idToken.longValue() == idActual;
    }

    private boolean versionValida(String token, UserDetails userDetails) {
        Number version = extractClaim(token, claims -> (Number) claims.get("versionSesion"));
        long actual = ((com.proyectocitas.model.Usuario) userDetails).getVersionSesion();
        return (version == null ? 0 : version.longValue()) == actual;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claimsResolver.apply(claims);
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
