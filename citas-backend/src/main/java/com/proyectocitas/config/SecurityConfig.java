package com.proyectocitas.config;

import com.proyectocitas.model.Permiso;
import com.proyectocitas.security.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import com.proyectocitas.security.CustomAccessDeniedHandler;
import com.proyectocitas.security.CustomAuthenticationEntryPoint;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;


    // =================================================
    // CONSTRUCTOR
    // =================================================
    // Recibe el filtro encargado de validar el JWT
    // enviado en cada petición.

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint ) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
         this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }


    // =================================================
    // CONFIGURACIÓN PRINCIPAL DE SEGURIDAD
    // =================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http)
            throws Exception {

        http

                // =================================================
                // CSRF
                // =================================================
                // Se desactiva porque utilizamos JWT
                // y no sesiones tradicionales.

                .csrf(csrf ->
                        csrf.disable()
                )


                // =================================================
                // CORS
                // =================================================
                // Permite que posteriormente Angular pueda
                // consumir la API desde otro origen.

                .cors(cors -> {
                })


                // =================================================
                // SESIONES
                // =================================================
                // Spring no guarda una sesión del usuario.
                // Cada petición debe enviar su JWT.

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )
                
                // =================================================
                // MANEJO DE ACCESO DENEGADO
                // =================================================
                // Personaliza la respuesta cuando el usuario
                // está autenticado pero no posee el permiso requerido.

                .exceptionHandling(exception ->
                        exception.accessDeniedHandler(
                                 customAccessDeniedHandler
                        )
                )
                
                 // =================================================
                 // MANEJO DE ERRORES DE SEGURIDAD
                 // =================================================
                 // 401: usuario no autenticado.
                 // 403: usuario autenticado pero sin permiso.

                     .exceptionHandling(exception ->
                          exception

                                .authenticationEntryPoint(
                                    customAuthenticationEntryPoint
                               )

                            .accessDeniedHandler(
                                        customAccessDeniedHandler
                               )
                    )


                // =================================================
                // AUTORIZACIÓN
                // =================================================

                .authorizeHttpRequests(auth -> auth


                        // =================================================
                        // PETICIONES OPTIONS
                        // =================================================
                        // Necesarias para peticiones CORS del navegador.

                        .requestMatchers(
                                HttpMethod.OPTIONS,
                                "/**"
                        )
                        .permitAll()


                        // =================================================
                        // AUTENTICACIÓN PÚBLICA
                        // =================================================
                        // Login y registro de pacientes no necesitan JWT.

                        .requestMatchers(
                                "/api/v1/auth/**"
                        )
                        .permitAll()


                        // =================================================
                        // ERRORES
                        // =================================================

                        .requestMatchers(
                                "/error"
                        )
                        .permitAll()
                        
// =================================================
// PERMISOS
// =================================================

.requestMatchers(
        HttpMethod.GET,
        "/api/v1/permissions"
)
.hasAuthority(
        Permiso.ASIGNAR_PERMISOS.name()
)

.requestMatchers(
        HttpMethod.PUT,
        "/api/v1/roles/*/permissions"
)
.hasAuthority(
        Permiso.ASIGNAR_PERMISOS.name()
)
                        // =================================================
                        // ASIGNACIÓN DE PERMISOS A ROLES
                        // =================================================
                        // Permite reemplazar los permisos de un rol.
                        //
                        // Se coloca ANTES del PUT general de roles.

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/roles/*/permissions"
                        )
                        .hasAuthority(
                                Permiso.ASIGNAR_PERMISOS.name()
                        )


                        // =================================================
                        // ASIGNACIÓN DE ROLES A USUARIOS
                        // =================================================
                        // Permite colocar uno o varios roles a un usuario.
                        //
                        // Se coloca ANTES del PUT general de usuarios.

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/users/*/roles"
                        )
                        .hasAuthority(
                                Permiso.ASIGNAR_ROLES.name()
                        )


                        // =================================================
                        // USUARIOS
                        // =================================================

                        // Crear nuevos usuarios.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/users"
                        )
                        .hasAuthority(
                                Permiso.CREAR_USUARIOS.name()
                        )


                        // Listar usuarios.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/users"
                        )
                        .hasAuthority(
                                Permiso.VER_USUARIOS.name()
                        )


                        // Consultar un usuario por ID.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/users/*"
                        )
                        .hasAuthority(
                                Permiso.VER_USUARIOS.name()
                        )


                        // Actualizar un usuario.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/users/*"
                        )
                        .hasAuthority(
                                Permiso.ACTUALIZAR_USUARIOS.name()
                        )


                        // Desactivar un usuario.
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/users/*"
                        )
                        .hasAuthority(
                                Permiso.DESACTIVAR_USUARIOS.name()
                        )


                        // =================================================
                        // ROLES
                        // =================================================

                        // Crear un nuevo rol.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/roles"
                        )
                        .hasAuthority(
                                Permiso.CREAR_ROLES.name()
                        )


                        // Listar todos los roles.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/roles"
                        )
                        .hasAuthority(
                                Permiso.VER_ROLES.name()
                        )


                        // Consultar un rol por ID.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/roles/*"
                        )
                        .hasAuthority(
                                Permiso.VER_ROLES.name()
                        )


                        // Actualizar un rol.
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/roles/*"
                        )
                        .hasAuthority(
                                Permiso.ACTUALIZAR_ROLES.name()
                        )


                        // Eliminar un rol.
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/v1/roles/*"
                        )
                        .hasAuthority(
                                Permiso.ELIMINAR_ROLES.name()
                        )


// =================================================
// ESPECIALIDADES
// =================================================

// Crear especialidad
.requestMatchers(
        HttpMethod.POST,
        "/api/v1/especialidades"
)
.hasAuthority(
        Permiso.CREAR_ESPECIALIDADES.name()
)

// Listar especialidades
.requestMatchers(
        HttpMethod.GET,
        "/api/v1/especialidades"
)
.hasAuthority(
        Permiso.VER_ESPECIALIDADES.name()
)

// Buscar especialidad por ID
.requestMatchers(
        HttpMethod.GET,
        "/api/v1/especialidades/*"
)
.hasAuthority(
        Permiso.VER_ESPECIALIDADES.name()
)

// Actualizar especialidad
.requestMatchers(
        HttpMethod.PUT,
        "/api/v1/especialidades/*"
)
.hasAuthority(
        Permiso.ACTUALIZAR_ESPECIALIDADES.name()
)

// Eliminar especialidad
.requestMatchers(
        HttpMethod.DELETE,
        "/api/v1/especialidades/*"
)
.hasAuthority(
        Permiso.ELIMINAR_ESPECIALIDADES.name()
)

                        // =================================================
                        // MÉDICOS
                        // =================================================

                        // Registrar un nuevo médico.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/medicos"
                        )
                        .hasAuthority(
                                Permiso.CREAR_MEDICOS.name()
                        )


                        // Consultar médicos.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/medicos/**"
                        )
                        .hasAuthority(
                                Permiso.VER_MEDICOS.name()
                        )
                        
                        // Actualizar médico
.requestMatchers(
        HttpMethod.PUT,
        "/api/v1/medicos/*"
)
.hasAuthority(
        Permiso.ACTUALIZAR_MEDICOS.name()
)


// Eliminar médico
.requestMatchers(
        HttpMethod.DELETE,
        "/api/v1/medicos/*"
)
.hasAuthority(
        Permiso.ELIMINAR_MEDICOS.name()
)


                        // =================================================
                        // HORARIOS
                        // =================================================

                        // Crear bloques de horarios disponibles.
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/schedules"
                        )
                        .hasAuthority(
                                Permiso.CREAR_HORARIOS.name()
                        )


                        // Consultar horarios disponibles.
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/schedules/available"
                        )
                        .hasAuthority(
                                Permiso.VER_HORARIOS_DISPONIBLES.name()
                        )


                        // =================================================
                        // HISTORIAL DEL PACIENTE
                        // =================================================
                        // Debe ir antes del GET general de citas.

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/appointments/my-history"
                        )
                        .hasAuthority(
                                Permiso.VER_HISTORIAL.name()
                        )


                        // =================================================
                        // CREAR CITA
                        // =================================================

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/appointments"
                        )
                        .hasAuthority(
                                Permiso.CREAR_CITA.name()
                        )


                        // =================================================
                        // CANCELAR CITA
                        // =================================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/appointments/*/cancel"
                        )
                        .hasAuthority(
                                Permiso.CANCELAR_CITA.name()
                        )


                        // =================================================
                        // REGISTRAR DIAGNÓSTICO
                        // =================================================

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/v1/appointments/*/diagnosis"
                        )
                        .hasAuthority(
                                Permiso.REGISTRAR_DIAGNOSTICO.name()
                        )


                        // =================================================
                        // VER CITAS
                        // =================================================
                        // Controla las consultas generales de citas.
                        //
                        // my-history ya fue declarado antes para que
                        // el paciente solamente necesite VER_HISTORIAL.
                        
                        .requestMatchers(
        HttpMethod.GET,
        "/api/v1/appointments",
        "/api/v1/appointments/**"
)
.hasAuthority(
        Permiso.VER_CITAS.name()
)


                        // =================================================
                        // RESTO DE ENDPOINTS
                        // =================================================
                        // Todo endpoint que todavía no tenga un permiso
                        // específico requiere al menos un JWT válido.

                        .anyRequest()
                        .authenticated()
                )


                // =================================================
                // FILTRO JWT
                // =================================================
                // Revisa el JWT antes de que Spring realice
                // la autenticación normal.

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    // =================================================
    // AUTHENTICATION MANAGER
    // =================================================
    // Se utiliza durante el login para comprobar
    // correo/usuario y contraseña.

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration)
            throws Exception {

        return authenticationConfiguration
                .getAuthenticationManager();
    }


    // =================================================
    // PASSWORD ENCODER
    // =================================================
    // BCrypt cifra las contraseñas antes
    // de almacenarlas en la base de datos.

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}