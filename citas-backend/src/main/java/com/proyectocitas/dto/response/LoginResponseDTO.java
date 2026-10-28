package com.proyectocitas.dto.response;


public class LoginResponseDTO {

    private boolean debeCambiarPassword;

    public boolean isDebeCambiarPassword() {
        return debeCambiarPassword;
    }

    public void setDebeCambiarPassword(boolean valor) {
        debeCambiarPassword = valor;
    }


    private String token;
    private String tipo;
    private LoginUsuarioResponseDTO usuario;


    // =================================================
    // CONSTRUCTOR
    // =================================================

    public LoginResponseDTO(
            String token,
            String tipo,
            LoginUsuarioResponseDTO usuario) {

        this.token = token;
        this.tipo = tipo;
        this.usuario = usuario;
    }


    // =================================================
    // GETTERS
    // =================================================

    public String getToken() {
        return token;
    }

    public String getTipo() {
        return tipo;
    }

    public LoginUsuarioResponseDTO getUsuario() {
        return usuario;
    }
}