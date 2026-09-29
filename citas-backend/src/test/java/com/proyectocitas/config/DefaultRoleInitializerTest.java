package com.proyectocitas.config;

import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class DefaultRoleInitializerTest {


    @Test
    void debeCrearRolesPrincipalesConPermisosCuandoNoExisten()
            throws Exception {

        // =================================================
        // REPOSITORY SIMULADO
        // =================================================

        RolRepository rolRepository =
                mock(RolRepository.class);


        // Simulamos una base de datos nueva sin roles.
        when(rolRepository.findAll())
                .thenReturn(List.of());


        List<Rol> rolesGuardados =
                new ArrayList<>();


        // Capturamos los roles que el inicializador intenta guardar.
        when(rolRepository.save(any(Rol.class)))
                .thenAnswer(invocation -> {

                    Rol rol =
                            invocation.getArgument(0);

                    rolesGuardados.add(rol);

                    return rol;
                });


        // =================================================
        // INICIALIZADOR
        // =================================================

        DefaultRoleInitializer initializer =
                new DefaultRoleInitializer(
                        rolRepository
                );


        // Ejecutamos la inicialización.
        initializer.run();


        // =================================================
        // VALIDAR ROLE_ADMIN
        // =================================================

        Rol admin = rolesGuardados
                .stream()
                .filter(rol ->
                        rol.getNombre()
                                .equals("ROLE_ADMIN")
                )
                .findFirst()
                .orElseThrow();


        assertTrue(
                admin.getPermisos()
                        .contains(
                                Permiso.CREAR_USUARIOS
                        )
        );

        assertTrue(
                admin.getPermisos()
                        .contains(
                                Permiso.ASIGNAR_PERMISOS
                        )
        );

        assertTrue(
                admin.getPermisos()
                        .contains(
                                Permiso.VER_ROLES
                        )
        );


        // =================================================
        // VALIDAR ROLE_DOCTOR
        // =================================================

        Rol doctor = rolesGuardados
                .stream()
                .filter(rol ->
                        rol.getNombre()
                                .equals("ROLE_DOCTOR")
                )
                .findFirst()
                .orElseThrow();


        assertTrue(
                doctor.getPermisos()
                        .contains(
                                Permiso.CREAR_HORARIOS
                        )
        );

        assertTrue(
                doctor.getPermisos()
                        .contains(
                                Permiso.REGISTRAR_DIAGNOSTICO
                        )
        );

        assertTrue(
                doctor.getPermisos()
                        .contains(
                                Permiso.VER_CITAS
                        )
        );


        // =================================================
        // VALIDAR ROLE_PATIENT
        // =================================================

        Rol patient = rolesGuardados
                .stream()
                .filter(rol ->
                        rol.getNombre()
                                .equals("ROLE_PATIENT")
                )
                .findFirst()
                .orElseThrow();


        assertTrue(
                patient.getPermisos()
                        .contains(
                                Permiso.CREAR_CITA
                        )
        );

        assertTrue(
                patient.getPermisos()
                        .contains(
                                Permiso.VER_HISTORIAL
                        )
        );

        assertTrue(
                patient.getPermisos()
                        .contains(
                                Permiso.CANCELAR_CITA
                        )
        );


        // =================================================
        // DEBEN CREARSE LOS 3 ROLES
        // =================================================

        verify(
                rolRepository,
                times(3)
        )
                .save(
                        any(Rol.class)
                );
    }
    
    @Test
void noDebeModificarPermisosDeUnRolExistente() {

    // =================================================
    // REPOSITORY SIMULADO
    // =================================================

    RolRepository rolRepository =
            mock(RolRepository.class);


    // =================================================
    // ROLE_DOCTOR EXISTENTE
    // =================================================
    // Simulamos que alguien quitó VER_CITAS manualmente.

    Rol doctorExistente =
            new Rol(
                    "ROLE_DOCTOR",
                    "Médico del sistema"
            );

    doctorExistente.setPermisos(
            Set.of(
                    Permiso.VER_MEDICOS,
                    Permiso.VER_ESPECIALIDADES,
                    Permiso.CREAR_HORARIOS,
                    Permiso.REGISTRAR_DIAGNOSTICO
            )
    );


    // =================================================
    // ROLES EXISTENTES
    // =================================================

    Rol adminExistente =
            new Rol(
                    "ROLE_ADMIN",
                    "Administrador"
            );

    Rol patientExistente =
            new Rol(
                    "ROLE_PATIENT",
                    "Paciente"
            );


    when(rolRepository.findAll())
            .thenReturn(
                    List.of(
                            adminExistente,
                            doctorExistente,
                            patientExistente
                    )
            );


    // =================================================
    // INICIALIZADOR
    // =================================================

    DefaultRoleInitializer initializer =
            new DefaultRoleInitializer(
                    rolRepository
            );


    initializer.run();


    // =================================================
    // VALIDACIONES
    // =================================================

    // VER_CITAS fue quitado anteriormente.
    // El inicializador NO debe volver a agregarlo.

    assertFalse(
            doctorExistente
                    .getPermisos()
                    .contains(
                            Permiso.VER_CITAS
                    )
    );


    // Como los tres roles ya existían,
    // no debe guardar ninguno nuevamente.

    verify(
            rolRepository,
            never()
    )
            .save(
                    any(Rol.class)
            );
}
    
    
    
    
    
}