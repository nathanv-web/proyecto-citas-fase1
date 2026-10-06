package com.proyectocitas.service.Impl;

import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceConflictException;
import com.proyectocitas.exception.ResourceNotFoundException;

import com.proyectocitas.model.Especialidad;

import com.proyectocitas.repository.EspecialidadRepository;

import com.proyectocitas.service.EspecialidadService;

import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EspecialidadServiceImpl
        implements EspecialidadService {

    private final EspecialidadRepository especialidadRepository;


    // =================================================
    // CONSTRUCTOR
    // =================================================

    public EspecialidadServiceImpl(
            EspecialidadRepository especialidadRepository
    ) {

        this.especialidadRepository =
                especialidadRepository;
    }


    // =================================================
    // LISTAR TODAS
    // =================================================

    @Override
    public List<Especialidad> obtenerTodos() {

        return especialidadRepository.findAll();
    }


    // =================================================
    // BUSCAR POR ID
    // =================================================

    @Override
    public Optional<Especialidad> obtenerPorId(Long id) {

        return especialidadRepository.findById(id);
    }


    // =================================================
    // BUSCAR POR NOMBRE
    // =================================================

    @Override
    public Optional<Especialidad> obtenerPorNombre(
            String nombre
    ) {

        if (
                nombre == null ||
                nombre.isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El nombre de la especialidad es obligatorio"
            );
        }

        return especialidadRepository
                .findFirstByNombreIgnoreCase(
                        nombre.trim()
                );
    }


    // =================================================
    // CREAR ESPECIALIDAD
    // =================================================

    @Override
    public Especialidad guardar(
            Especialidad especialidad
    ) {

        // Validar objeto
        if (especialidad == null) {

            throw new IllegalArgumentException(
                    "Los datos de la especialidad son obligatorios"
            );
        }


        // Validar nombre
        if (
                especialidad.getNombre() == null ||
                especialidad.getNombre().isBlank()
        ) {

            throw new IllegalArgumentException(
                    "El nombre de la especialidad es obligatorio"
            );
        }


        // Limpiar nombre
        String nombre =
                especialidad
                        .getNombre()
                        .trim();


        // Verificar duplicado
        if (
                especialidadRepository
                        .existsByNombreIgnoreCase(nombre)
        ) {

            throw new DuplicateResourceException(
                    "Ya existe una especialidad con el nombre: "
                            + nombre
            );
        }


        especialidad.setNombre(nombre);


        // Limpiar descripción si existe
        if (
                especialidad.getDescripcion() != null
        ) {

            especialidad.setDescripcion(
                    especialidad
                            .getDescripcion()
                            .trim()
            );
        }


        return especialidadRepository
                .save(especialidad);
    }


    // =================================================
    // ACTUALIZAR ESPECIALIDAD
    // =================================================

    @Override
    public Especialidad actualizar(
            Especialidad especialidad,
            Long id
    ) {

        // Validar ID
        if (id == null) {

            throw new IllegalArgumentException(
                    "El ID de la especialidad es obligatorio"
            );
        }


        // Validar body
        if (especialidad == null) {

            throw new IllegalArgumentException(
                    "Los datos de la especialidad son obligatorios"
            );
        }


        // Buscar especialidad actual
        Especialidad especialidadDB =
                especialidadRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No existe la especialidad con ID: "
                                                + id
                                )
                        );


        // =================================================
        // ACTUALIZAR NOMBRE
        // =================================================

        if (
                especialidad.getNombre() != null &&
                !especialidad.getNombre().isBlank()
        ) {

            String nombre =
                    especialidad
                            .getNombre()
                            .trim();


            // Comprobar que otra especialidad
            // no tenga el mismo nombre.
            if (
                    especialidadRepository
                            .existsByNombreIgnoreCaseAndIdEspecialidadNot(
                                    nombre,
                                    id
                            )
            ) {

                throw new DuplicateResourceException(
                        "Ya existe una especialidad con el nombre: "
                                + nombre
                );
            }


            especialidadDB.setNombre(nombre);
        }


        // =================================================
        // ACTUALIZAR DESCRIPCIÓN
        // =================================================

        if (
                especialidad.getDescripcion() != null
        ) {

            especialidadDB.setDescripcion(
                    especialidad
                            .getDescripcion()
                            .trim()
            );
        }


        // =================================================
        // ACTUALIZAR ESTADO
        // =================================================

        if (
                especialidad.getActivo() != null
        ) {

            especialidadDB.setActivo(
                    especialidad.getActivo()
            );
        }


        return especialidadRepository
                .save(especialidadDB);
    }


    // =================================================
    // ELIMINAR ESPECIALIDAD
    // =================================================

    @Override
    @Transactional
    public void eliminar(Long id) {

        if (id == null) {

            throw new IllegalArgumentException(
                    "El ID de la especialidad es obligatorio"
            );
        }


        Especialidad especialidad =
                especialidadRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No existe la especialidad con ID: "
                                                + id
                                )
                        );


        try {

            especialidadRepository
                    .delete(especialidad);

            /*
             * Fuerza a Hibernate a ejecutar el DELETE aquí,
             * para poder capturar errores de llave foránea.
             */
            especialidadRepository.flush();

        } catch (DataIntegrityViolationException ex) {

            throw new ResourceConflictException(
                    "No se puede eliminar la especialidad "
                            + "porque está siendo utilizada por uno o más médicos"
            );
        }
    }
}