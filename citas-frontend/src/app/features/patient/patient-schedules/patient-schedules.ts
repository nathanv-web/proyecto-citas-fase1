import { ChangeDetectorRef, Component, inject, signal } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { AppointmentService, Horario } from '../appointment.service';
import { finalize } from 'rxjs';

/** El componente busca horarios disponibles para un médico y una fecha. */
@Component({
  selector: 'app-patient-schedules',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './patient-schedules.html',
})
export class PatientSchedules {
  private readonly changeDetector = inject(ChangeDetectorRef);

  private service = inject(AppointmentService);

  schedules = signal<Horario[]>([]);

  idMedico?: number;

  fecha = '';

  message = '';

  search() {
    if (!this.idMedico || !this.fecha) {
      this.message = 'Ingrese médico y fecha';

      return;
    }

    this.service
      .getAvailableSchedules(this.idMedico, this.fecha)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (data) => {
          this.schedules.set(data);

          this.message = data.length === 0 ? 'No hay horarios disponibles' : '';
        },

        error: (error) => {
          console.error(error);

          this.message = error.error?.message ?? 'No se pudieron consultar los horarios';
        },
      });
  }
}
