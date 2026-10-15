package com.proyectocitas.controller;

import com.proyectocitas.mapper.EspecialidadMapper;
import com.proyectocitas.model.Especialidad;
import com.proyectocitas.repository.EspecialidadRepository;
import com.proyectocitas.service.Impl.EspecialidadServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EspecialidadEstadoTest {
    private EspecialidadRepository repository;
    private MockMvc mvc;
    private Especialidad especialidad;

    @BeforeEach
    void setUp() {
        repository = mock(EspecialidadRepository.class);
        mvc = MockMvcBuilders.standaloneSetup(new EspecialidadController(
                new EspecialidadServiceImpl(repository), new EspecialidadMapper())).build();
        especialidad = new Especialidad("Cardiología", "Especialidad del corazón");
        especialidad.setIdEspecialidad(1L);
        especialidad.setActivo(false);
        when(repository.findById(1L)).thenReturn(Optional.of(especialidad));
        when(repository.save(especialidad)).thenReturn(especialidad);
    }

    @Test
    void reactivaConSoloElEstadoSinCambiarNombreNiDescripcion() throws Exception {
        mvc.perform(patch("/api/v1/especialidades/1/estado")
                .contentType(MediaType.APPLICATION_JSON).content("{\"activo\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.nombre").value("Cardiología"))
                .andExpect(jsonPath("$.descripcion").value("Especialidad del corazón"));
        assertTrue(especialidad.getActivo());
        verify(repository).save(especialidad);
        verify(repository, never()).delete(any());
    }

    @Test
    void rechazaEstadoNuloSinModificarElRegistro() throws Exception {
        mvc.perform(patch("/api/v1/especialidades/1/estado")
                .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        assertFalse(especialidad.getActivo());
        verify(repository, never()).save(any());
    }

    @Test
    void desactivaYConservaLaEspecialidadParaReactivarla() throws Exception {
        especialidad.setActivo(true);
        mvc.perform(delete("/api/v1/especialidades/1")).andExpect(status().isNoContent());
        assertFalse(especialidad.getActivo());
        verify(repository).save(especialidad);
        verify(repository, never()).delete(any());
    }
}
