package com.proyectocitas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class JsonInvalidoHandlerTest {

    @Test
    void debeResponder400CuandoJsonEstaMalFormado() throws Exception {

        MockMvc mockMvc =
                MockMvcBuilders
                        .standaloneSetup(new TestController())
                        .setControllerAdvice(
                                new GlobalExceptionHandler()
                        )
                        .build();


        String jsonInvalido = """
                {
                    "nombre": "Jaime"
                    "edad": 19
                }
                """;


        mockMvc.perform(
                post("/test/json-invalido")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido)
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
                                .value("El formato de la solicitud JSON es inválido")
                )
                .andExpect(
                        jsonPath("$.path")
                                .value("/test/json-invalido")
                );
    }


    @RestController
    static class TestController {

        @PostMapping("/test/json-invalido")
        public void recibir(
                @RequestBody TestRequest request) {
        }
    }


    static class TestRequest {

        private String nombre;
        private Integer edad;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Integer getEdad() {
            return edad;
        }

        public void setEdad(Integer edad) {
            this.edad = edad;
        }
    }
}