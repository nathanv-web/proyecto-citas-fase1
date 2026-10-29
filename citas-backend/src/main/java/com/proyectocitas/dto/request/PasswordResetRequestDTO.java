package com.proyectocitas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PasswordResetRequestDTO {
    @NotBlank(message = "El token es obligatorio")
    @Pattern(regexp = "[A-Za-z0-9_-]{43}", message = "El enlace no es válido o ya venció. Solicita uno nuevo.")
    private String token;
    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Size(min = 8, max = 64, message = "La contraseña debe tener entre 8 y 64 caracteres")
    private String nueva;

    public String getToken() { return token; }
    public void setToken(String value) { token = value; }
    public String getNueva() { return nueva; }
    public void setNueva(String value) { nueva = value; }
}
