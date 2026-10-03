package com.proyectocitas.service;

import com.proyectocitas.exception.ResourceNotFoundException;
import com.proyectocitas.model.*;
import com.proyectocitas.repository.*;
import com.proyectocitas.service.Impl.*;

import org.hibernate.ResourceClosedException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ServiceImplCoverageTest {

    // =================================================
    // MEDICO SERVICE
    // =================================================

    @Test
    void medicoOperacionesBasicas() {

        MedicoRepository medicoRepo = mock(MedicoRepository.class);
        UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);
        EspecialidadRepository especialidadRepo =
                mock(EspecialidadRepository.class);

        MedicoServiceImpl service =
                new MedicoServiceImpl(
                        medicoRepo,
                        usuarioRepo,
                        especialidadRepo
                );

        Medico medico = new Medico();

        when(medicoRepo.findAll())
                .thenReturn(List.of(medico));

        when(medicoRepo.findById(1L))
                .thenReturn(Optional.of(medico));

        when(medicoRepo.findByUsuario_IdUsuario(5L))
                .thenReturn(Optional.of(medico));

        when(medicoRepo.save(medico))
                .thenReturn(medico);


        assertEquals(1, service.obtenerTodos().size());

        assertTrue(
                service.obtenerPorId(1L).isPresent()
        );

        assertTrue(
                service.obtenerPorUsuario(5L).isPresent()
        );

        assertSame(
                medico,
                service.guardar(medico)
        );

        service.eliminar(1L);

        verify(medicoRepo).deleteById(1L);
    }


    @Test
    void medicoActualizarCompleto() {

        MedicoRepository medicoRepo = mock(MedicoRepository.class);
        UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);
        EspecialidadRepository especialidadRepo =
                mock(EspecialidadRepository.class);

        MedicoServiceImpl service =
                new MedicoServiceImpl(
                        medicoRepo,
                        usuarioRepo,
                        especialidadRepo
                );


        Medico existente = new Medico();

        Usuario referenciaUsuario = new Usuario();
        referenciaUsuario.setIdUsuario(2L);

        Usuario usuarioDB = new Usuario();
        usuarioDB.setIdUsuario(2L);


        Especialidad referenciaEspecialidad =
                new Especialidad();

        referenciaEspecialidad.setIdEspecialidad(3L);

        Especialidad especialidadDB =
                new Especialidad();

        especialidadDB.setIdEspecialidad(3L);


        Medico cambios = new Medico();

        cambios.setUsuario(referenciaUsuario);
        cambios.setEspecialidad(referenciaEspecialidad);
        cambios.setColegiado("COL-500");
        cambios.setAniosExperiencia(10);
        cambios.setBiografia("Especialista");
        cambios.setEstado(Medico.EstadoMedico.ACTIVO);


        when(medicoRepo.findById(1L))
                .thenReturn(Optional.of(existente));

        when(usuarioRepo.findById(2L))
                .thenReturn(Optional.of(usuarioDB));

        when(especialidadRepo.findById(3L))
                .thenReturn(Optional.of(especialidadDB));

        when(medicoRepo.save(existente))
                .thenReturn(existente);


        Medico resultado =
                service.actualizar(cambios, 1L);


        assertSame(existente, resultado);
        assertSame(usuarioDB, existente.getUsuario());
        assertSame(
                especialidadDB,
                existente.getEspecialidad()
        );

        assertEquals(
                "COL-500",
                existente.getColegiado()
        );

        assertEquals(
                10,
                existente.getAniosExperiencia()
        );

        assertEquals(
                "Especialista",
                existente.getBiografia()
        );

        assertEquals(
                Medico.EstadoMedico.ACTIVO,
                existente.getEstado()
        );
    }


    @Test
    void medicoActualizarInexistente() {

        MedicoRepository medicoRepo = mock(MedicoRepository.class);

        MedicoServiceImpl service =
                new MedicoServiceImpl(
                        medicoRepo,
                        mock(UsuarioRepository.class),
                        mock(EspecialidadRepository.class)
                );

        when(medicoRepo.findById(99L))
                .thenReturn(Optional.empty());


        assertThrows(
                ResourceClosedException.class,
                () -> service.actualizar(
                        new Medico(),
                        99L
                )
        );
    }


    @Test
    void medicoActualizarUsuarioInexistente() {

        MedicoRepository medicoRepo = mock(MedicoRepository.class);
        UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);

        MedicoServiceImpl service =
                new MedicoServiceImpl(
                        medicoRepo,
                        usuarioRepo,
                        mock(EspecialidadRepository.class)
                );


        Usuario usuario = new Usuario();
        usuario.setIdUsuario(50L);

        Medico cambios = new Medico();
        cambios.setUsuario(usuario);


        when(medicoRepo.findById(1L))
                .thenReturn(Optional.of(new Medico()));

        when(usuarioRepo.findById(50L))
                .thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> service.actualizar(
                        cambios,
                        1L
                )
        );
    }


    @Test
    void medicoActualizarEspecialidadInexistente() {

        MedicoRepository medicoRepo = mock(MedicoRepository.class);
        EspecialidadRepository especialidadRepo =
                mock(EspecialidadRepository.class);

        MedicoServiceImpl service =
                new MedicoServiceImpl(
                        medicoRepo,
                        mock(UsuarioRepository.class),
                        especialidadRepo
                );


        Especialidad especialidad =
                new Especialidad();

        especialidad.setIdEspecialidad(99L);

        Medico cambios = new Medico();
        cambios.setEspecialidad(especialidad);


        when(medicoRepo.findById(1L))
                .thenReturn(Optional.of(new Medico()));

        when(especialidadRepo.findById(99L))
                .thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> service.actualizar(
                        cambios,
                        1L
                )
        );
    }


  
    // =================================================
    // ESPECIALIDAD SERVICE
    // =================================================

    @Test
    void especialidadOperacionesBasicas() {

        EspecialidadRepository repo =
                mock(EspecialidadRepository.class);

        EspecialidadServiceImpl service =
                new EspecialidadServiceImpl(repo);

        Especialidad especialidad =
                new Especialidad();


        when(repo.findAll())
                .thenReturn(List.of(especialidad));

        when(repo.findById(1L))
                .thenReturn(Optional.of(especialidad));

        when(repo.findByNombre("Cardiologia"))
                .thenReturn(Optional.of(especialidad));

        when(repo.save(especialidad))
                .thenReturn(especialidad);


        assertEquals(
                1,
                service.obtenerTodos().size()
        );

        assertTrue(
                service.obtenerPorId(1L).isPresent()
        );

        assertTrue(
                service.obtenerPorNombre(
                        "Cardiologia"
                ).isPresent()
        );

        assertSame(
                especialidad,
                service.guardar(especialidad)
        );


        service.eliminar(1L);

        verify(repo).deleteById(1L);
    }


    @Test
    void especialidadActualizarCompleto() {

        EspecialidadRepository repo =
                mock(EspecialidadRepository.class);

        EspecialidadServiceImpl service =
                new EspecialidadServiceImpl(repo);


        Especialidad existente =
                new Especialidad();

        Especialidad cambios =
                new Especialidad();

        cambios.setNombre("Neurologia");
        cambios.setDescripcion("Sistema nervioso");
        cambios.setActivo(true);


        when(repo.findById(1L))
                .thenReturn(Optional.of(existente));

        when(repo.save(existente))
                .thenReturn(existente);


        Especialidad resultado =
                service.actualizar(
                        cambios,
                        1L
                );


        assertSame(existente, resultado);

        assertEquals(
                "Neurologia",
                existente.getNombre()
        );

        assertEquals(
                "Sistema nervioso",
                existente.getDescripcion()
        );

        assertTrue(existente.getActivo());
    }


    @Test
    void especialidadActualizarInexistente() {

        EspecialidadRepository repo =
                mock(EspecialidadRepository.class);

        EspecialidadServiceImpl service =
                new EspecialidadServiceImpl(repo);


        when(repo.findById(99L))
                .thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> service.actualizar(
                        new Especialidad(),
                        99L
                )
        );
    }


    // =================================================
    // ESTADO CITA SERVICE
    // =================================================

    @Test
    void estadoCitaOperacionesBasicas() {

        EstadoCitaRepository repo =
                mock(EstadoCitaRepository.class);

        EstadoCitaServiceImpl service =
                new EstadoCitaServiceImpl(repo);

        EstadoCita estado = new EstadoCita();


        when(repo.findAll())
                .thenReturn(List.of(estado));

        when(repo.findById(1L))
                .thenReturn(Optional.of(estado));

        when(repo.findByNombre(
                EstadoCita.NombreEstado.PENDIENTE))
                .thenReturn(Optional.of(estado));

        when(repo.save(estado))
                .thenReturn(estado);


        assertEquals(
                1,
                service.obtenerTodos().size()
        );

        assertTrue(
                service.obtenerPorId(1L).isPresent()
        );

        assertTrue(
                service.obtenerPorNombre(
                        EstadoCita.NombreEstado.PENDIENTE
                ).isPresent()
        );

        assertSame(
                estado,
                service.guardar(estado)
        );


        service.eliminar(1L);

        verify(repo).deleteById(1L);
    }


    @Test
    void estadoCitaActualizarCompleto() {

        EstadoCitaRepository repo =
                mock(EstadoCitaRepository.class);

        EstadoCitaServiceImpl service =
                new EstadoCitaServiceImpl(repo);


        EstadoCita existente =
                new EstadoCita();

        EstadoCita cambios =
                new EstadoCita();

        cambios.setNombre(
                EstadoCita.NombreEstado.COMPLETADA
        );

        cambios.setDescripcion(
                "Cita completada"
        );


        when(repo.findById(1L))
                .thenReturn(Optional.of(existente));

        when(repo.save(existente))
                .thenReturn(existente);


        EstadoCita resultado =
                service.actualizar(
                        cambios,
                        1L
                );


        assertSame(existente, resultado);

        assertEquals(
                EstadoCita.NombreEstado.COMPLETADA,
                existente.getNombre()
        );

        assertEquals(
                "Cita completada",
                existente.getDescripcion()
        );
    }


    @Test
    void estadoCitaActualizarInexistente() {

        EstadoCitaRepository repo =
                mock(EstadoCitaRepository.class);

        EstadoCitaServiceImpl service =
                new EstadoCitaServiceImpl(repo);


        when(repo.findById(99L))
                .thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> service.actualizar(
                        new EstadoCita(),
                        99L
                )
        );
    }
}