package com.proyectocitas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class PruebaController {

    @GetMapping("/hola")
    public String saludar() {
        return "¡El backend de Citas está funcionando correctamente!";
    }
}