package com.proyectocitas.exception;

import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class IdentityConcurrencyErrorTest {
    @Test
    void conflictoDeVersionDevuelve409SinDatosInternos() throws Exception {
        MockMvcBuilders.standaloneSetup(new ConflictingEndpoint())
                .setControllerAdvice(new GlobalExceptionHandler()).build()
                .perform(put("/identity-concurrency-test"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("El registro cambió durante la operación. Actualiza los datos e intenta nuevamente."))
                .andExpect(jsonPath("$.details").doesNotExist());
    }

    @RestController
    static class ConflictingEndpoint {
        @PutMapping("/identity-concurrency-test")
        public void update() { throw new OptimisticLockingFailureException("detalle-interno-no-publicar"); }
    }
}
