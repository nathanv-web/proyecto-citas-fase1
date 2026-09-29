package com.proyectocitas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ResourceConflictHandlerTest {

    @Test
    void debeResponder409CuandoExisteConflictoDeRecurso() throws Exception {

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(new TestController())
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();


        mockMvc.perform(
                get("/test/resource-conflict")
        )
                .andExpect(
                        status().isConflict()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(409)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Conflict")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Solo se pueden cancelar citas pendientes")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/resource-conflict")
                );
    }


    @RestController
    static class TestController {

        @GetMapping("/test/resource-conflict")
        public void lanzarExcepcion() {

            throw new ResourceConflictException(
                    "Solo se pueden cancelar citas pendientes"
            );
        }
    }
}