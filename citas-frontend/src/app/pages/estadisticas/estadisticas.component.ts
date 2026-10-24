import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-estadisticas',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './estadisticas.component.html',
  styleUrl: './estadisticas.component.css'
})
export class EstadisticasComponent {
  // Métricas clave del Dashboard Administrativo (KPIs)
  totalUsuarios: number = 124;
  totalMedicos: number = 18;
  totalCitasMes: number = 340;
  rolesActivos: number = 3;
}