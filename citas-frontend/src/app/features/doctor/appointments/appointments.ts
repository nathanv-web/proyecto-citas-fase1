import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Appointment } from '../../../models/appointment.model';
import { DoctorService } from '../../../services/doctor';

@Component({
  selector: 'app-appointments',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './appointments.html',
  styleUrl: './appointments.css'
})
export class Appointments {

  private readonly doctorService = inject(DoctorService);

  appointments: Appointment[] = [];
  cargando = true;
  error = '';

  ngOnInit(): void {
    this.cargarCitas();
  }

  cargarCitas(): void {
    this.cargando = true;
    this.error = '';

    this.doctorService.getAppointments().subscribe({
      next: (data) => {
        this.appointments = data;
        this.cargando = false;
      },
      error: (error) => {
        console.error(error);
        this.error = 'No se pudieron cargar las citas.';
        this.cargando = false;
      }
    });
  }
}
