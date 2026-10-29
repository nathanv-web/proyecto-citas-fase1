package com.proyectocitas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class EmailVerifyRequestDTO {
    @NotBlank(message = "El token es obligatorio")
    @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "El enlace de verificación no es válido o ya venció.")
    private String token;
    public String getToken() { return token; }
    public void setToken(String value) { token = value; }
}
