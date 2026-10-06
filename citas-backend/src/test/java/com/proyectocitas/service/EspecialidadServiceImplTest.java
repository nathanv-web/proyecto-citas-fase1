package com.proyectocitas.service;

import com.proyectocitas.exception.DuplicateResourceException;
import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.Especialidad;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.service.Impl.EspecialidadServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EspecialidadServiceImplTest {

    private EspecialidadRepository repo;
    private EspecialidadServiceImpl service;


    // =================================================
    // CONFIGURACIÓN
    // =================================================

    @BeforeEach
    void setUp() {

        repo = mock(EspecialidadRepository.class);

        service =
                new EspecialidadServiceImpl(repo);
    }


    // =================================================
    // LISTAR
    // =================================================

    @Test
    void debeListarEspecialidades() {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(1L);
        especialidad.setNombre("Cardiologia");
        especialidad.setDescripcion(
                "Especialidad del corazón"
        );
        especialidad.setActivo(true);


        when(repo.findAll())
                .thenReturn(
                        List.of(especialidad)
                );


        List<Especialidad> resultado =
                service.obtenerTodos();


        assertNotNull(resultado);

        assertEquals(
                1,
                resultado.size()
        );

        assertEquals(
                "Cardiologia",
                resultado.get(0).getNombre()
        );


        verify(repo).findAll();
    }


    // =================================================
    // BUSCAR POR ID
    // =================================================

    @Test
    void debeBuscarEspecialidadPorId() {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(1L);
        especialidad.setNombre("Cardiologia");


        when(repo.findById(1L))
                .thenReturn(
                        Optional.of(especialidad)
                );


        Optional<Especialidad> resultado =
                service.obtenerPorId(1L);


        assertTrue(resultado.isPresent());

        assertEquals(
                "Cardiologia",
                resultado.get().getNombre()
        );


        verify(repo).findById(1L);
    }


    // =================================================
    // BUSCAR POR NOMBRE
    // =================================================

    @Test
    void debeBuscarEspecialidadPorNombre() {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(1L);
        especialidad.setNombre("Cardiologia");


        when(
                repo.findFirstByNombreIgnoreCase(
                        "Cardiologia"
                )
        ).thenReturn(
                Optional.of(especialidad)
        );


        Optional<Especialidad> resultado =
                service.obtenerPorNombre(
                        "Cardiologia"
                );


        assertTrue(resultado.isPresent());

        assertEquals(
                1L,
                resultado.get()
                        .getIdEspecialidad()
        );


        verify(repo)
                .findFirstByNombreIgnoreCase(
                        "Cardiologia"
                );
    }


    // =================================================
    // CREAR
    // =================================================

    @Test
    void debeGuardarEspecialidad() {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(1L);
        especialidad.setNombre("Neurologia");
        especialidad.setDescripcion(
                "Sistema nervioso"
        );
        especialidad.setActivo(true);


        when(
                repo.existsByNombreIgnoreCase(
                        "Neurologia"
                )
        ).thenReturn(false);


        when(repo.save(especialidad))
                .thenReturn(especialidad);


        Especialidad resultado =
                service.guardar(
                        especialidad
                );


        assertNotNull(resultado);

        assertEquals(
                "Neurologia",
                resultado.getNombre()
        );


        verify(repo)
                .existsByNombreIgnoreCase(
                        "Neurologia"
                );

        verify(repo)
                .save(especialidad);
    }


    // =================================================
    // NOMBRE OBLIGATORIO
    // =================================================

    @Test
    void debeLanzarErrorSiNombreEsNulo() {

        Especialidad especialidad =
                new Especialidad();


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service.guardar(
                                especialidad
                        )
        );


        verify(
                repo,
                never()
        ).save(any());
    }


    // =================================================
    // NOMBRE DUPLICADO
    // =================================================

    @Test
    void debeLanzarErrorSiNombreYaExiste() {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setNombre(
                "Cardiologia"
        );


        when(
                repo.existsByNombreIgnoreCase(
                        "Cardiologia"
                )
        ).thenReturn(true);


        assertThrows(
                DuplicateResourceException.class,
                () ->
                        service.guardar(
                                especialidad
                        )
        );


        verify(
                repo,
                never()
        ).save(any());
    }


    // =================================================
    // ACTUALIZAR
    // =================================================

    @Test
    void debeActualizarEspecialidad() {

        Especialidad existente =
                new Especialidad();

        existente.setIdEspecialidad(1L);
        existente.setNombre("Cardiologia");
        existente.setDescripcion(
                "Descripcion anterior"
        );
        existente.setActivo(true);


        Especialidad cambios =
                new Especialidad();

        cambios.setNombre(
                "Cardiologia Clinica"
        );

        cambios.setDescripcion(
                "Nueva descripcion"
        );

        cambios.setActivo(true);


        when(repo.findById(1L))
                .thenReturn(
                        Optional.of(existente)
                );


        when(
                repo.existsByNombreIgnoreCaseAndIdEspecialidadNot(
                        "Cardiologia Clinica",
                        1L
                )
        ).thenReturn(false);


        when(repo.save(existente))
                .thenReturn(existente);


        Especialidad resultado =
                service.actualizar(
                        cambios,
                        1L
                );


        assertNotNull(resultado);

        assertEquals(
                "Cardiologia Clinica",
                resultado.getNombre()
        );

        assertEquals(
                "Nueva descripcion",
                resultado.getDescripcion()
        );

        assertTrue(
                resultado.getActivo()
        );


        verify(repo)
                .save(existente);
    }


    // =================================================
    // ACTUALIZAR INEXISTENTE
    // =================================================

    @Test
    void debeLanzarErrorSiEspecialidadNoExiste() {

        Especialidad cambios =
                new Especialidad();

        cambios.setNombre(
                "Neurologia"
        );


        when(repo.findById(99L))
                .thenReturn(
                        Optional.empty()
                );


        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        service.actualizar(
                                cambios,
                                99L
                        )
        );


        verify(
                repo,
                never()
        ).save(any());
    }


    // =================================================
    // ACTUALIZAR CON NOMBRE DUPLICADO
    // =================================================

    @Test
    void debeLanzarErrorSiActualizaConNombreDuplicado() {

        Especialidad existente =
                new Especialidad();

        existente.setIdEspecialidad(1L);
        existente.setNombre("Cardiologia");


        Especialidad cambios =
                new Especialidad();

        cambios.setNombre("Neurologia");


        when(repo.findById(1L))
                .thenReturn(
                        Optional.of(existente)
                );


        when(
                repo.existsByNombreIgnoreCaseAndIdEspecialidadNot(
                        "Neurologia",
                        1L
                )
        ).thenReturn(true);


        assertThrows(
                DuplicateResourceException.class,
                () ->
                        service.actualizar(
                                cambios,
                                1L
                        )
        );


        verify(
                repo,
                never()
        ).save(any());
    }


    // =================================================
    // ELIMINAR
    // =================================================

    @Test
    void debeEliminarEspecialidad() {

        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(1L);
        especialidad.setNombre(
                "Cardiologia"
        );


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


    // =================================================
    // ELIMINAR INEXISTENTE
    // =================================================

    @Test
    void debeLanzarErrorAlEliminarEspecialidadInexistente() {

        when(repo.findById(99L))
                .thenReturn(
                        Optional.empty()
                );


        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        service.eliminar(
                                99L
                        )
        );


        verify(
                repo,
                never()
        ).delete(any());
    }
}