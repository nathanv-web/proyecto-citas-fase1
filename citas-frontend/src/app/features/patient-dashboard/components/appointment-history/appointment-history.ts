import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppointmentService } from '../../../../services/appointment';

@Component({
  selector: 'app-appointment-history',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './appointment-history.html',
  styleUrl: './appointment-history.css'
})
export class AppointmentHistoryComponent implements OnInit {
  historial: any[] = [];

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
    this.cargarHistorial();
  }

  cargarHistorial() {
    this.appointmentService.getHistorial().subscribe({
      next: (datos) => this.historial = datos,
      error: (err) => console.error('Error al cargar el historial', err)
    });
  }

  cancelarCita(id: number) {
    if (confirm('¿Estás seguro de que deseas cancelar esta cita?')) {
      this.appointmentService.cancelarCita(id).subscribe({
        next: () => {
          alert('Cita cancelada con éxito');
          this.cargarHistorial(); // Recargamos la tabla para actualizar los datos
        },
        error: (err) => console.error('Error al cancelar la cita', err)
      });
    }
  }
}