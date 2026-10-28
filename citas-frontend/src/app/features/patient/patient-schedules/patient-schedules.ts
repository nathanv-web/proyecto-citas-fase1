import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { AppointmentService, Horario, Medico } from '../appointment.service';

/** La disponibilidad se consulta eligiendo al médico por nombre y especialidad. */
@Component({
  selector: 'app-patient-schedules',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './patient-schedules.html',
})
export class PatientSchedules implements OnInit {
  private readonly service = inject(AppointmentService);

  readonly doctors = signal<Medico[]>([]);
  readonly schedules = signal<Horario[]>([]);
  readonly loadingDoctors = signal(false);
  readonly loading = signal(false);
  readonly message = signal('');

  idMedico = 0;
  fecha = '';

  ngOnInit(): void {
    this.loadingDoctors.set(true);
    this.service
      .getDoctors('', '', 'ACTIVO')
      .pipe(finalize(() => this.loadingDoctors.set(false)))
      .subscribe({ next: (doctors) => this.doctors.set(doctors), error: () => {} });
  }

  clearSchedules(): void {
    this.schedules.set([]);
    this.message.set('');
  }

  search(): void {
    if (!this.idMedico || !this.fecha || this.loading()) return;
    this.clearSchedules();
    this.loading.set(true);
    const medico = this.idMedico;
    const fecha = this.fecha;
    this.service
      .getAvailableSchedules(medico, fecha)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (schedules) => {
          if (this.idMedico !== medico || this.fecha !== fecha) return;
          this.schedules.set(schedules);
          this.message.set(schedules.length ? '' : 'No hay horarios disponibles para esta fecha.');
        },
        error: () => {},
      });
  }
}
