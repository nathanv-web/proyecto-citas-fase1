package com.proyectocitas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IllegalArgumentHandlerTest {

    @Test
    void debeResponder400CuandoArgumentoEsInvalido() throws Exception {

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(new TestController())
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();

        mockMvc.perform(
                get("/test/argumento-invalido")
        )
                .andExpect(
                        status().isBadRequest()
                )
                .andExpect(
                        jsonPath("$.status")
                                .value(400)
                )
                .andExpect(
                        jsonPath("$.error")
                                .value("Bad Request")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("La fecha es obligatoria")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/argumento-invalido")
                );
    }


    @RestController
    static class TestController {

        @GetMapping("/test/argumento-invalido")
        public void lanzarExcepcion() {

            throw new IllegalArgumentException(
                    "La fecha es obligatoria"
            );
        }
    }
}