import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { AppointmentService } from '../../../../services/appointment';

@Component({
  selector: 'app-appointment-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './appointment-form.html',
  styleUrl: './appointment-form.css'
})
export class AppointmentFormComponent {
  
  datosReserva = {
    idHorario: '',
    motivo: ''
  };

  mensaje = '';

  constructor(private appointmentService: AppointmentService) {}

  reservarCita() {
    if (!this.datosReserva.idHorario) {
      this.mensaje = 'Por favor, ingresa el ID del horario que deseas.';
      return;
    }

    
    this.appointmentService.crearCita(this.datosReserva).subscribe({
      next: (respuesta) => {
        this.mensaje = '¡Cita reservada con éxito!';
        this.datosReserva = { idHorario: '', motivo: '' }; 
      },
      error: (err) => {
        console.error('Error al reservar', err);
        this.mensaje = 'Ocurrió un error al intentar reservar la cita.';
      }
    });
  }
}