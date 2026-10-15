import { Component, inject, OnInit, signal } from '@angular/core';

import { AppointmentService, Medico } from '../appointment.service';

/** El directorio consulta los perfiles médicos disponibles y presenta sus datos profesionales. */
@Component({
  selector: 'app-patient-doctors',
  standalone: true,
  templateUrl: './patient-doctors.html',
})
export class PatientDoctors implements OnInit {
  private service = inject(AppointmentService);

  doctors = signal<Medico[]>([]);

  ngOnInit(): void {
    this.service.getDoctors().subscribe({
      next: (data) => {
        this.doctors.set(data);
      },

      error: (error) => {
        console.error('Error cargando médicos', error);
      },
    });
  }
}
