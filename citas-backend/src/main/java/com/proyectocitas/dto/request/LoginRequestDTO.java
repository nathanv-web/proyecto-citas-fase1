package com.proyectocitas.dto.request;

public class LoginRequestDTO {

    @jakarta.validation.constraints.NotBlank(message = "El correo es obligatorio")
    @jakarta.validation.constraints.Email(message = "El correo debe tener un formato válido")
    private String email;
    @jakarta.validation.constraints.NotBlank(message = "La contraseña es obligatoria")
    private String password;

    // --- Constructores ---
    public LoginRequestDTO() {}

    // --- Getters y Setters ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}