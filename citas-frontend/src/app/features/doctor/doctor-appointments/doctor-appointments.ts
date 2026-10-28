import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';

import { DoctorAppointment, DoctorService } from '../doctor.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';

/** El médico elige la cita por paciente, fecha y hora dentro de su propia agenda. */
@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './doctor-appointments.html',
})
export class DoctorAppointments implements OnInit {
  private readonly notifications = inject(NotificationService);
  private readonly authService = inject(AuthService);
  private readonly service = inject(DoctorService);

  readonly canViewAgenda = this.authService.hasPermission('VER_CITAS');
  readonly canRegisterDiagnosis = this.authService.hasPermission('REGISTRAR_DIAGNOSTICO');
  readonly appointments = signal<DoctorAppointment[]>([]);
  readonly pendingAppointments = computed(() =>
    this.appointments().filter((cita) => cita.estado === 'PENDIENTE'),
  );
  readonly loading = signal(false);
  readonly saving = signal(false);

  fecha = new Date().toLocaleDateString('en-CA');
  periodo = 'dia';
  appointmentId?: number;
  diagnosticoReceta = '';
  observaciones = '';

  ngOnInit(): void {
    this.loadAgenda();
  }

  loadAgenda(): void {
    if ((!this.canViewAgenda && !this.canRegisterDiagnosis) || this.loading() || this.saving())
      return;
    this.selectAppointment(undefined);
    this.appointments.set([]);
    this.loading.set(true);
    this.service
      .getAgenda(this.fecha, this.periodo)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({ next: (appointments) => this.appointments.set(appointments), error: () => {} });
  }

  selectAppointment(id?: number): void {
    this.appointmentId = id;
    this.diagnosticoReceta = '';
    this.observaciones = '';
  }

  saveDiagnosis(): void {
    if (
      !this.canRegisterDiagnosis ||
      this.loading() ||
      this.saving() ||
      !this.diagnosticoReceta.trim()
    )
      return;
    const selected = this.pendingAppointments().find((cita) => cita.idCita === this.appointmentId);
    if (!selected) return;
    this.saving.set(true);
    this.service
      .registerDiagnosis(selected.idCita, this.diagnosticoReceta, this.observaciones)
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.notifications.success('Diagnóstico registrado correctamente');
          this.loadAgenda();
        },
        error: () => this.saving.set(false),
      });
  }
}
