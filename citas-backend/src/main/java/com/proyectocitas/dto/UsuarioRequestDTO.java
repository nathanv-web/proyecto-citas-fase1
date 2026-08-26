package com.proyectocitas.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UsuarioRequestDTO {

    //Nombre del usuario
    @NotBlank(message = "El nombre es obligatorio")
    @Size(
        min = 2,
        max = 100,
        message = "El nombre debe tener entre 2 y 100 caracteres"
    )
    private String nombre;

    //Apellido del usuario
    @NotBlank(message = "El apellido es obligatorio")
    @Size(
        min = 2,
        max = 100,
        message = "El apellido debe tener entre 2 y 100 caracteres"
    )
    private String apellido;

    //Correo del usuario
    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe tener un formato válido")
    private String correo;

    //Teléfono del usuario
    @Size(
        max = 20,
        message = "El teléfono no puede superar los 20 caracteres"
    )
    private String telefono;

    //Contraseña del usuario
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(
        min = 8,
        max = 12,
        message = "La contraseña debe tener entre 8 y 12 caracteres"
    )
    private String contrasena;


    public UsuarioRequestDTO() {
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}