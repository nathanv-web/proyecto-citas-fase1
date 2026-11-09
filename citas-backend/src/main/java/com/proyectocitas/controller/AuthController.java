package com.proyectocitas.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;


import com.proyectocitas.dto.request.LoginRequestDTO;
import com.proyectocitas.security.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.proyectocitas.dto.response.LoginResponseDTO;
import com.proyectocitas.dto.response.LoginUsuarioResponseDTO;

import java.util.HashSet;
import java.util.Set;
import com.proyectocitas.dto.response.UsuarioResponseDTO;
import com.proyectocitas.dto.request.UsuarioCreateRequestDTO;
import com.proyectocitas.mapper.UsuarioMapper;
import com.proyectocitas.model.Usuario;
import com.proyectocitas.service.UsuarioService;
import com.proyectocitas.service.EmailVerificationService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;


@Tag(name = "Autenticación", description = "Inicio de sesión y registro público de pacientes.")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsuarioService usuarioService;
    private final UsuarioMapper usuarioMapper;
    private final EmailVerificationService emailVerification;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UsuarioService usuarioService,
            UsuarioMapper usuarioMapper,
            EmailVerificationService emailVerification
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usuarioService = usuarioService;
        this.usuarioMapper = usuarioMapper;
        this.emailVerification = emailVerification;

    }

   @Operation(
       summary = "Iniciar sesión",
       description = "Autentica una cuenta activa mediante correo y contraseña. Devuelve el JWT, su tipo, los datos " +
           "del usuario, sus roles, permisos y el indicador de cambio de contraseña obligatorio.",
       requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
           description = "Correo y contraseña de la cuenta. Los datos del ejemplo son ficticios.",
           required = true, useParameterTypeSchema = true,
           content = @Content(mediaType = "application/json", examples = @ExampleObject(
               name = "Ejemplo", value = "{\"email\": \"ana@example.com\", \"password\": \"EjemploClave123\"}"))),
       responses = {
           @ApiResponse(responseCode = "200", description = "Consulta u operación realizada correctamente.",
               content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponseDTO.class))),
           @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
           @ApiResponse(responseCode = "401", ref = "#/components/responses/Error401"),
           @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
       }
   )
   @PostMapping("/login")
public ResponseEntity<LoginResponseDTO> login(
        @Valid @RequestBody LoginRequestDTO loginRequest
) {

    // =================================================
    // AUTENTICAR CREDENCIALES
    // =================================================
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
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
                    usuario
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

    LoginUsuarioResponseDTO usuarioLogin =
            new LoginUsuarioResponseDTO(
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
    respuesta.setDebeCambiarPassword(usuario.isDebeCambiarPassword());
    usuarioLogin.setCorreoVerificado(usuario.isCorreoVerificado());
    usuarioLogin.setTelefono(usuario.getTelefono());
    return ResponseEntity.ok(
            respuesta
    );
}
    @Operation(
        summary = "Registrar paciente",
        description = "Registra una cuenta con el rol ROLE_PATIENT y solicita el envío de un enlace de verificación de " +
            "correo.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del paciente. Nombre y apellido comienzan con mayúscula y no admiten espacios; teléfono " +
                "de 8 a 15 dígitos y contraseña de 8 a 64 caracteres.",
            required = true, useParameterTypeSchema = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(
                name = "Ejemplo", value = "{\"nombre\": \"Ana\", \"apellido\": \"Lopez\", \"correo\": \"ana@example.com\", \"telefono\": \"55554444\", \"contrasena\": \"EjemploClave123\"}"))),
        responses = {
            @ApiResponse(responseCode = "201", description = "Registro creado correctamente.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UsuarioResponseDTO.class))),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/Error400"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Error409"),
            @ApiResponse(responseCode = "500", ref = "#/components/responses/Error500")
        }
    )
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(
    @Valid
    @RequestBody UsuarioCreateRequestDTO usuarioRequestDTO){

        // Convertir DTO de entrada a entidad de Usuario
        Usuario usuario = usuarioMapper.toEntity(usuarioRequestDTO);

        //Registrar usuario asignado automaticamente como ROLE_PATIENT
        Usuario usuarioguardado = usuarioService.registrarPaciente(usuario);
        emailVerification.requestAfterRegistration(usuarioguardado.getCorreo());

        //Convertir entidad guardada a DTO de respuesta
        UsuarioResponseDTO usuarioDTO = usuarioMapper.toDTO(usuarioguardado);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(usuarioDTO);
    }
}

