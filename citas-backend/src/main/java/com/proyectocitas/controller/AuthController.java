package com.proyectocitas.controller;

import com.proyectocitas.dto.LoginRequest;
import com.proyectocitas.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.proyectocitas.dto.LoginResponseDTO;
import com.proyectocitas.dto.LoginUsuarioDTO;

import java.util.HashSet;
import java.util.Set;
import com.proyectocitas.dto.UsuarioDTO;
import com.proyectocitas.dto.UsuarioRequestDTO;
import com.proyectocitas.mapper.UsuarioMapper;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.service.UsuarioService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;


@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        
    }

   @PostMapping("/login")
public ResponseEntity<LoginResponseDTO> login(
        @RequestBody LoginRequest loginRequest
) {

    // =================================================
    // AUTENTICAR CREDENCIALES
    // =================================================
    Authentication authentication =
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );
    // =================================================
    // USUARIO AUTENTICADO
    // =================================================

    Usuario usuario =
            (Usuario) authentication.getPrincipal();
    // =================================================
    // GENERAR JWT
    // =================================================
    String jwt =
            jwtService.generateToken(
                    loginRequest.getEmail()
            );
    // =================================================
    // OBTENER ROLES Y PERMISOS
    // =================================================
    Set<String> roles =
            new HashSet<>();
    Set<String> permisos =
            new HashSet<>();
    usuario.getRoles().forEach(rol -> {
        roles.add(
                rol.getNombre()
        );
        if (rol.getPermisos() != null) {
            rol.getPermisos().forEach(
                    permiso ->
                            permisos.add(
                                    permiso.name()
                            )
            );
        }
    });
    // =================================================
    // INFORMACIÓN DEL USUARIO
    // =================================================

    LoginUsuarioDTO usuarioLogin =
            new LoginUsuarioDTO(
                    usuario.getIdUsuario(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getCorreo(),
                    roles,
                    permisos
            );
    // =================================================
    // RESPUESTA DEL LOGIN
    // =================================================

    LoginResponseDTO respuesta =
            new LoginResponseDTO(
                    jwt,
                    "Bearer",
                    usuarioLogin
            );
    return ResponseEntity.ok(
            respuesta
    );
}
    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(
    @Valid 
    @RequestBody UsuarioRequestDTO usuarioRequestDTO){
        
        // Convertir DTO de entrada a entidad de Usuario
        Usuario usuario = usuarioMapper.toEntity(usuarioRequestDTO);
        
        //Registrar usuario asignado automaticamente como ROLE_PATIENT
        Usuario usuarioguardado = usuarioService.registrarPaciente(usuario);
        
        //Convertir entidad guardada a DTO de respuesta
        UsuarioDTO usuarioDTO = usuarioMapper.toDTO(usuarioguardado);
        
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioDTO);
    }
}

