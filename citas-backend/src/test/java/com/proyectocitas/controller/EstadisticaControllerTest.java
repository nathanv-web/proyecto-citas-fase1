package com.proyectocitas.controller;

import com.proyectocitas.dto.response.EstadisticaResponseDTO;
import com.proyectocitas.service.EstadisticaService;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EstadisticaControllerTest {
    @Test
    void devuelveResumenDelMesActualSinExponerDatosPersonales() throws Exception {
        var service = mock(EstadisticaService.class);
        when(service.obtenerResumen(any(YearMonth.class)))
                .thenReturn(new EstadisticaResponseDTO(5, 2, 9, 3, YearMonth.now().toString()));
        var mvc = MockMvcBuilders.standaloneSetup(new EstadisticaController(service)).build();
        mvc.perform(get("/api/v1/admin/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsuarios").value(5))
                .andExpect(jsonPath("$.totalMedicos").value(2))
                .andExpect(jsonPath("$.totalCitasMes").value(9))
                .andExpect(jsonPath("$.totalRoles").value(3))
                .andExpect(jsonPath("$.mes").value(YearMonth.now().toString()))
                .andExpect(jsonPath("$.correo").doesNotExist());
        verify(service).obtenerResumen(YearMonth.now());
    }
}
