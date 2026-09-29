package com.proyectocitas.config;

import com.proyectocitas.model.Permiso;
import com.proyectocitas.model.Rol;
import com.proyectocitas.repository.RolRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Component
public class DefaultRoleInitializer
        implements CommandLineRunner {

    private final RolRepository rolRepository;


    // =================================================
    // CONSTRUCTOR
    // =================================================

    public DefaultRoleInitializer(
            RolRepository rolRepository) {

        this.rolRepository = rolRepository;
    }


    // =================================================
    // INICIALIZACIÓN DE ROLES PRINCIPALES
    // =================================================
    // Se ejecuta cuando Spring Boot inicia.
    //
    // Solo crea ROLE_ADMIN, ROLE_DOCTOR y ROLE_PATIENT
    // cuando todavía no existen.
    //
    // Si ya existen, NO modifica sus permisos.

    @Override
    public void run(String... args) {

        List<Rol> rolesExistentes =
                rolRepository.findAll();


        Set<String> nombresExistentes =
                rolesExistentes
                        .stream()
                        .map(Rol::getNombre)
                        .collect(Collectors.toSet());


        // =================================================
        // ROLE_ADMIN
        // =================================================

        if (!nombresExistentes.contains("ROLE_ADMIN")) {

            Rol admin = new Rol(
                    "ROLE_ADMIN",
                    "Personal encargado de gestión administrativa y control de citas"
            );

            admin.setPermisos(
                    Set.of(
                            Permiso.CREAR_USUARIOS,
                            Permiso.VER_USUARIOS,
                            Permiso.ACTUALIZAR_USUARIOS,
                            Permiso.DESACTIVAR_USUARIOS,

                            Permiso.CREAR_ROLES,
                            Permiso.VER_ROLES,
                            Permiso.ACTUALIZAR_ROLES,
                            Permiso.ELIMINAR_ROLES,
                            Permiso.ASIGNAR_ROLES,
                            Permiso.ASIGNAR_PERMISOS,

                            Permiso.CREAR_MEDICOS,
                            Permiso.VER_MEDICOS,

                            Permiso.CREAR_ESPECIALIDADES,
                            Permiso.VER_ESPECIALIDADES,

                            Permiso.CREAR_HORARIOS,
                            Permiso.VER_HORARIOS_DISPONIBLES,

                            Permiso.VER_CITAS,
                            Permiso.REGISTRAR_DIAGNOSTICO
                    )
            );

            rolRepository.save(admin);
        }


        // =================================================
        // ROLE_DOCTOR
        // =================================================

        if (!nombresExistentes.contains("ROLE_DOCTOR")) {

            Rol doctor = new Rol(
                    "ROLE_DOCTOR",
                    "Médico del sistema"
            );

            doctor.setPermisos(
                    Set.of(
                            Permiso.VER_MEDICOS,
                            Permiso.VER_ESPECIALIDADES,
                            Permiso.CREAR_HORARIOS,
                            Permiso.VER_CITAS,
                            Permiso.REGISTRAR_DIAGNOSTICO
                    )
            );

            rolRepository.save(doctor);
        }


        // =================================================
        // ROLE_PATIENT
        // =================================================

        if (!nombresExistentes.contains("ROLE_PATIENT")) {

            Rol patient = new Rol(
                    "ROLE_PATIENT",
                    "Paciente del sistema"
            );

            patient.setPermisos(
                    Set.of(
                            Permiso.VER_MEDICOS,
                            Permiso.VER_ESPECIALIDADES,
                            Permiso.VER_HORARIOS_DISPONIBLES,
                            Permiso.CREAR_CITA,
                            Permiso.VER_HISTORIAL,
                            Permiso.CANCELAR_CITA
                    )
            );

            rolRepository.save(patient);
        }
    }
}