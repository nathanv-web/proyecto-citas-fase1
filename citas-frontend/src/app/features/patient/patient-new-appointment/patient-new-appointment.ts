import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { AppointmentService, Horario, Medico } from '../appointment.service';
import { NotificationService } from '../../../core/notifications/notification.service';

@Component({
  selector: 'app-patient-new-appointment',
  standalone: true,
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './patient-new-appointment.html',
})
export class PatientNewAppointment implements OnInit {
  private readonly service = inject(AppointmentService);
  private readonly notifications = inject(NotificationService);
  private readonly fb = inject(FormBuilder);
  readonly doctors = signal<Medico[]>([]);
  readonly schedules = signal<Horario[]>([]);
  readonly saving = signal(false);
  readonly searching = signal(false);
  idMedico = 0;
  fecha = '';
  message = '';
  readonly form = this.fb.nonNullable.group({
    idHorario: [0, [Validators.required, Validators.min(1)]],
    motivo: ['', [Validators.required, Validators.minLength(5)]],
  });

  ngOnInit(): void {
    this.service
      .getDoctors('', '', 'ACTIVO')
      .subscribe({ next: (data) => this.doctors.set(data), error: () => {} });
  }
  clearSchedules(): void {
    this.schedules.set([]);
    this.form.controls.idHorario.setValue(0);
    this.message = '';
  }
  searchSchedules(): void {
    this.clearSchedules();
    if (!this.idMedico || !this.fecha) return;
    this.searching.set(true);
    const medicoConsultado = this.idMedico;
    const fechaConsultada = this.fecha;
    this.service
      .getAvailableSchedules(medicoConsultado, fechaConsultada)
      .pipe(finalize(() => this.searching.set(false)))
      .subscribe({
        next: (data) => {
          if (this.idMedico !== medicoConsultado || this.fecha !== fechaConsultada) return;
          this.schedules.set(data);
          this.message = data.length ? '' : 'No hay horarios disponibles para esta fecha.';
        },
        error: () => {},
      });
  }
  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving()) return;
    const { idHorario, motivo } = this.form.getRawValue();
    this.saving.set(true);
    this.service
      .createAppointment(idHorario, motivo)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Cita creada correctamente');
          this.form.reset();
          this.searchSchedules();
        },
        error: (error) => {
          if (error.status === 409) this.searchSchedules();
        },
      });
  }
}
