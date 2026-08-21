package com.proyectocitas.controller;

import com.proyectocitas.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    // Aquí le decimos a la ventanilla que escuche en la ruta "/login" usando el método POST
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        
        // 1. Verificamos que el correo y la contraseña sean correctos
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(), 
                        loginRequest.getPassword()
                )
        );

        // 2. Aquí generaremos el gafete VIP (JWT)
        // Por ahora pondremos un texto de prueba hasta conectar con el código del Integrante 2
        String jwt = "AQUI_IRA_EL_TOKEN_JWT_VERDADERO"; 

        // 3. Le entregamos el token al usuario
        return ResponseEntity.ok(jwt);
    }
}