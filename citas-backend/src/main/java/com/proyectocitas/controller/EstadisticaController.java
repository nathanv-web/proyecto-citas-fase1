package com.proyectocitas.controller;

import com.proyectocitas.dto.response.EstadisticaResponseDTO;
import com.proyectocitas.service.EstadisticaService;
import java.time.YearMonth;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/statistics")
public class EstadisticaController {
    private final EstadisticaService estadisticaService;

    public EstadisticaController(EstadisticaService estadisticaService) {
        this.estadisticaService = estadisticaService;
    }

    @GetMapping
    public ResponseEntity<EstadisticaResponseDTO> obtenerResumen() {
        return ResponseEntity.ok(estadisticaService.obtenerResumen(YearMonth.now()));
    }
}
