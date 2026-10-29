package com.proyectocitas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmailVerificationRequestDTO {
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 255, message = "El correo no puede superar los 255 caracteres")
    private String email;
    public String getEmail() { return email; }
    public void setEmail(String value) { email = value; }
}
