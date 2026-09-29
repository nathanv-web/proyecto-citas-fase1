package com.proyectocitas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ScheduleConflictHandlerTest {

    @Test
    void debeResponder409CuandoExisteConflictoDeHorario() throws Exception {

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(new TestController())
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();

        mockMvc.perform(
                get("/test/schedule-conflict")
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
                                .value("El horario ya fue reservado")
                )

                .andExpect(
                        jsonPath("$.path")
                                .value("/test/schedule-conflict")
                );
    }


@RestController
static class TestController {

    @GetMapping("/test/schedule-conflict")
    public void lanzarExcepcion() {

        throw new ScheduleConflictException(
                "El horario ya fue reservado"
        );
    }
}
}