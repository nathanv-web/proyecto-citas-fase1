package com.proyectocitas.dto;

import com.proyectocitas.dto.request.UsuarioUpdateRequestDTO;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioUpdateValidationTest {
    @Test
    void aceptaCorreoValidoYRechazaApellidoConNumeros() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var request = new UsuarioUpdateRequestDTO();
            request.setCorreo("ana@example.com");
            request.setApellido("López");
            assertTrue(validator.validate(request).isEmpty());
            request.setApellido("López123");
            assertTrue(validator.validate(request).stream()
                    .anyMatch(error -> error.getPropertyPath().toString().equals("apellido")));
        }
    }
}
