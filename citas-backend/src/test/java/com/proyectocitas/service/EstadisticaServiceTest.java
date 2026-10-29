package com.proyectocitas.service;

import com.proyectocitas.repository.*;
import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EstadisticaServiceTest {
    @Test
    void cuentaRegistrosRealesYUsaElMesDelHorarioIncluyendoUltimoDia() {
        var usuarios = mock(UsuarioRepository.class);
        var medicos = mock(MedicoRepository.class);
        var citas = mock(CitaRepository.class);
        var roles = mock(RolRepository.class);
        when(usuarios.count()).thenReturn(124L);
        when(medicos.count()).thenReturn(18L);
        when(roles.count()).thenReturn(3L);
        when(citas.countByHorario_FechaBetween(LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29))).thenReturn(340L);
        var result = new EstadisticaService(usuarios, medicos, citas, roles).obtenerResumen(YearMonth.of(2028, 2));
        assertEquals(124L, result.getTotalUsuarios());
        assertEquals(18L, result.getTotalMedicos());
        assertEquals(340L, result.getTotalCitasMes());
        assertEquals(3L, result.getTotalRoles());
        assertEquals("2028-02", result.getMes());
        verify(citas).countByHorario_FechaBetween(LocalDate.of(2028, 2, 1), LocalDate.of(2028, 2, 29));
        verify(citas, never()).findAll();
    }
}
