package com.proyectocitas.exception;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class GlobalExceptionHandlerTest {

    @Test
    void debeDevolverErrorPersonalizado() throws Exception {

        MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(new TestController())
        .setControllerAdvice(
                new GlobalExceptionHandler()
        )
        .build();

        mockMvc.perform(
                get("/test/conflict")
        )
        .andExpect(status().isConflict())
        .andExpect(
                jsonPath("$.status").value(409)
        )
        .andExpect(
                jsonPath("$.message")
                        .value("Horario no disponible")
        );
    }


    @RestController
    static class TestController {

        @GetMapping("/test/conflict")
        public void generarError() {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Horario no disponible"
            );
        }
    }
}