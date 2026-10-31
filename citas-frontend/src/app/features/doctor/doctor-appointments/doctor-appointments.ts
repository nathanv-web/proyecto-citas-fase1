import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';

import { DoctorAppointment, DoctorService } from '../doctor.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { apiErrorMessage } from '../../../core/notifications/api-error';

/** El médico elige la cita por paciente, fecha y hora dentro de su propia agenda. */
@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './doctor-appointments.html',
})
export class DoctorAppointments implements OnInit {
  private readonly notifications = inject(NotificationService);
  private readonly authService = inject(AuthService);
  private readonly service = inject(DoctorService);
  private readonly route = inject(ActivatedRoute);

  readonly diagnosisMode = this.route.snapshot.data['diagnosisMode'] === true;
  private readonly requestedAppointmentId =
    Number(this.route.snapshot.queryParamMap.get('cita')) || undefined;

  readonly canViewAgenda = this.authService.hasPermission('VER_CITAS');
  readonly canRegisterDiagnosis = this.authService.hasPermission('REGISTRAR_DIAGNOSTICO');
  readonly appointments = signal<DoctorAppointment[]>([]);
  readonly pendingAppointments = computed(() =>
    this.appointments().filter((cita) => cita.estado === 'PENDIENTE'),
  );
  readonly loading = signal(false);
  readonly saving = signal(false);
  readonly agendaError = signal('');
  readonly diagnosisError = signal('');

  fecha = this.route.snapshot.queryParamMap.get('fecha') || new Date().toLocaleDateString('en-CA');
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
    this.agendaError.set('');
    if (!this.fecha) {
      this.agendaError.set('Selecciona una fecha para consultar tu agenda.');
      return;
    }
    this.loading.set(true);
    this.service
      .getAgenda(this.fecha, this.periodo)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (appointments) => {
          this.appointments.set(appointments);
          if (
            this.diagnosisMode &&
            this.requestedAppointmentId &&
            this.pendingAppointments().some((cita) => cita.idCita === this.requestedAppointmentId)
          ) {
            this.selectAppointment(this.requestedAppointmentId);
          }
        },
        error: (error) => this.agendaError.set(apiErrorMessage(error)),
      });
  }

  selectAppointment(id?: number): void {
    this.appointmentId = id;
    this.diagnosticoReceta = '';
    this.observaciones = '';
    this.diagnosisError.set('');
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
    if (
      this.diagnosticoReceta.trim().length < 5 ||
      this.diagnosticoReceta.trim().length > 5000 ||
      this.observaciones.length > 2000
    ) {
      this.diagnosisError.set(
        'El diagnóstico debe tener entre 5 y 5000 caracteres y las observaciones hasta 2000.',
      );
      return;
    }
    this.diagnosisError.set('');
    this.saving.set(true);
    this.service
      .registerDiagnosis(selected.idCita, this.diagnosticoReceta.trim(), this.observaciones.trim())
      .subscribe({
        next: () => {
          this.saving.set(false);
          this.notifications.success('Diagnóstico registrado correctamente');
          this.loadAgenda();
        },
        error: (error) => {
          this.diagnosisError.set(apiErrorMessage(error));
          this.saving.set(false);
        },
      });
  }
}
