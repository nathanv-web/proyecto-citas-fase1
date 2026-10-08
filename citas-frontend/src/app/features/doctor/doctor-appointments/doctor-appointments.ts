import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  FormsModule
} from '@angular/forms';

import { DoctorService } from '../doctor.service';

@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [FormsModule],
  templateUrl:
    './doctor-appointments.html'
})
export class DoctorAppointments {

  private service =
    inject(DoctorService);

  appointments =
    signal<any[]>([]);
appointmentId?: number;

diagnosticoReceta = '';

observaciones = '';

saveDiagnosis() {

  if (
    !this.appointmentId ||
    !this.diagnosticoReceta.trim()
  ) {
    return;
  }

  this.service
    .registerDiagnosis(
      this.appointmentId,
      this.diagnosticoReceta,
      this.observaciones
    )
    .subscribe({

      next: () => {

        alert(
          'Diagnóstico registrado correctamente'
        );

        this.diagnosticoReceta = '';
        this.observaciones = '';
      },

      error: error => {

        console.error(error);

        alert(
          error.error?.message
          ?? 'No se pudo registrar el diagnóstico'
        );
      }

    });
}}