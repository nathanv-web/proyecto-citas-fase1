package com.proyectocitas.service;

import com.proyectocitas.model.Especialidad;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.service.Impl.EspecialidadServiceImpl;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceImplCoverageTest {


    // =================================================
    // ESPECIALIDADES
    // =================================================

    @Test
    void especialidadOperacionesBasicas() {

        EspecialidadRepository repo =
                mock(EspecialidadRepository.class);


        EspecialidadServiceImpl service =
                new EspecialidadServiceImpl(repo);


        // =================================================
        // ESPECIALIDAD EXISTENTE
        // =================================================

        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(1L);

        especialidad.setNombre(
                "Cardiologia"
        );

        especialidad.setDescripcion(
                "Especialidad del corazón"
        );

        especialidad.setActivo(true);


        // =================================================
        // LISTAR
        // =================================================

        when(repo.findAll())
                .thenReturn(
                        List.of(especialidad)
                );


        List<Especialidad> lista =
                service.obtenerTodos();


        assertNotNull(lista);

        assertEquals(
                1,
                lista.size()
        );

        assertEquals(
                "Cardiologia",
                lista.get(0)
                        .getNombre()
        );


        // =================================================
        // BUSCAR POR ID
        // =================================================

        when(repo.findById(1L))
                .thenReturn(
                        Optional.of(especialidad)
                );


        Optional<Especialidad> porId =
                service.obtenerPorId(1L);


        assertTrue(
                porId.isPresent()
        );

        assertEquals(
                1L,
                porId.get()
                        .getIdEspecialidad()
        );


        // =================================================
        // BUSCAR POR NOMBRE
        // =================================================

        when(
                repo.findFirstByNombreIgnoreCase(
                        "Cardiologia"
                )
        ).thenReturn(
                Optional.of(especialidad)
        );


        Optional<Especialidad> porNombre =
                service.obtenerPorNombre(
                        "Cardiologia"
                );


        assertTrue(
                porNombre.isPresent()
        );

        assertEquals(
                "Cardiologia",
                porNombre.get()
                        .getNombre()
        );


        // =================================================
        // CREAR
        // =================================================

        Especialidad nueva =
                new Especialidad();

        nueva.setIdEspecialidad(2L);

        nueva.setNombre(
                "Neurologia"
        );

        nueva.setDescripcion(
                "Especialidad del sistema nervioso"
        );

        nueva.setActivo(true);


        when(
                repo.existsByNombreIgnoreCase(
                        "Neurologia"
                )
        ).thenReturn(false);


        when(repo.save(nueva))
                .thenReturn(nueva);


        Especialidad guardada =
                service.guardar(
                        nueva
                );


        assertNotNull(
                guardada
        );

        assertEquals(
                "Neurologia",
                guardada.getNombre()
        );


        // =================================================
        // ACTUALIZAR
        // =================================================

        Especialidad cambios =
                new Especialidad();

        cambios.setNombre(
                "Cardiologia Clinica"
        );

        cambios.setDescripcion(
                "Atencion especializada del corazon"
        );

        cambios.setActivo(true);


        when(repo.findById(1L))
                .thenReturn(
                        Optional.of(especialidad)
                );


        when(
                repo.existsByNombreIgnoreCaseAndIdEspecialidadNot(
                        "Cardiologia Clinica",
                        1L
                )
        ).thenReturn(false);


        when(repo.save(especialidad))
                .thenReturn(especialidad);


        Especialidad actualizada =
                service.actualizar(
                        cambios,
                        1L
                );


        assertNotNull(
                actualizada
        );

        assertEquals(
                "Cardiologia Clinica",
                actualizada.getNombre()
        );

        assertEquals(
                "Atencion especializada del corazon",
                actualizada.getDescripcion()
        );

        assertTrue(
                actualizada.getActivo()
        );


        // =================================================
        // ELIMINAR
        // =================================================

        when(repo.findById(1L))
                .thenReturn(
                        Optional.of(especialidad)
                );


        service.eliminar(1L);


        verify(repo)
                .delete(especialidad);

        verify(repo)
                .flush();
    }
}