import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';

import { DoctorService } from '../doctor.service';
import { AppointmentService, Medico } from '../../patient/appointment.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';

/** El horario pertenece al médico autenticado, seleccionado por su perfil profesional. */
@Component({
  selector: 'app-doctor-schedules',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './doctor-schedules.html',
})
export class DoctorSchedules implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly service = inject(DoctorService);
  private readonly appointments = inject(AppointmentService);
  private readonly auth = inject(AuthService);
  private readonly notifications = inject(NotificationService);

  readonly doctors = signal<Medico[]>([]);
  readonly loadingDoctors = signal(false);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    idMedico: [0, [Validators.required, Validators.min(1)]],
    fecha: ['', Validators.required],
    horaInicio: ['', Validators.required],
    horaFin: ['', Validators.required],
  });

  ngOnInit(): void {
    const correo = this.auth.getUsuario()?.correo;
    if (!correo) return;
    this.loadingDoctors.set(true);
    this.appointments
      .getDoctors(correo)
      .pipe(finalize(() => this.loadingDoctors.set(false)))
      .subscribe({
        next: (doctors) => {
          const ownDoctors = doctors.filter(
            (doctor) => doctor.correo.toLowerCase() === correo.toLowerCase(),
          );
          this.doctors.set(ownDoctors);
          if (ownDoctors.length === 1) this.form.controls.idMedico.setValue(ownDoctors[0].idMedico);
        },
        error: () => {},
      });
  }

  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving() || this.loadingDoctors()) return;
    const { idMedico, fecha, horaInicio, horaFin } = this.form.getRawValue();
    if (!this.doctors().some((doctor) => doctor.idMedico === idMedico)) return;
    this.saving.set(true);
    this.service
      .createSchedule(idMedico, fecha, horaInicio, horaFin)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Horario creado correctamente');
          this.form.reset({ idMedico });
        },
        error: () => {},
      });
  }
}
