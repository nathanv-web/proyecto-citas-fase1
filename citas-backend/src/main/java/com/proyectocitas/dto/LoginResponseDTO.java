package com.proyectocitas.dto;


public class LoginResponseDTO {

    private String token;
    private String tipo;
    private LoginUsuarioDTO usuario;


    // =================================================
    // CONSTRUCTOR
    // =================================================

    public LoginResponseDTO(
            String token,
            String tipo,
            LoginUsuarioDTO usuario) {

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

    public LoginUsuarioDTO getUsuario() {
        return usuario;
    }
}