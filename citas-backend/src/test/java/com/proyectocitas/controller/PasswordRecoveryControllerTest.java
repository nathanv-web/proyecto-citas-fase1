package com.proyectocitas.controller;

import com.proyectocitas.exception.GlobalExceptionHandler;
import com.proyectocitas.service.PasswordRecoveryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class PasswordRecoveryControllerTest {
    private final PasswordRecoveryService service = mock(PasswordRecoveryService.class);
    private MockMvc mvc;
    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new PasswordRecoveryController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void respuestaGenericaIdenticaSinTokenParaTodosLosCorreos() throws Exception {
        String known = mvc.perform(post("/api/v1/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ana@example.com\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        String unknown = mvc.perform(post("/api/v1/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                .header("X-Forwarded-For", "203.0.113.50")
                .content("{\"email\":\"nadie@example.com\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertEquals(known, unknown);
        verify(service).requestRecovery("nadie@example.com", "127.0.0.1");
    }

    @Test
    void validaCorreoYResetSinDevolverValoresSensibles() throws Exception {
        mvc.perform(post("/api/v1/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"no-es-correo\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.email").exists());
        mvc.perform(post("/api/v1/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"invalido\",\"nueva\":\"abc\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors.token").exists())
                .andExpect(jsonPath("$.errors.nueva").exists()).andExpect(jsonPath("$.token").doesNotExist());
        verifyNoInteractions(service);
    }

    @Test
    void restableceYDevuelveMensajeSinAutenticarAutomaticamente() throws Exception {
        mvc.perform(post("/api/v1/auth/password/reset").contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\":\"" + "A".repeat(43) + "\",\"nueva\":\"NuevaClave123\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.token").doesNotExist());
        verify(service).resetPassword("A".repeat(43), "NuevaClave123", "127.0.0.1");
    }

    @Test
    void conserva429YElMensajeSeguroDelLimitador() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Intenta más tarde"))
                .when(service).requestRecovery(anyString(), anyString());
        mvc.perform(post("/api/v1/auth/password/forgot").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"nadie@example.com\"}"))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.status").value(429));
    }
}
