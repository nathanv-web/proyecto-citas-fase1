import { ChangeDetectorRef, Component, inject, OnInit, signal } from '@angular/core';

import { FormsModule } from '@angular/forms';

import { DoctorAppointment, DoctorService } from '../doctor.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { finalize } from 'rxjs';

/** El componente consulta la agenda y registra diagnósticos de acuerdo con los permisos de la cuenta. */
@Component({
  selector: 'app-doctor-appointments',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './doctor-appointments.html',
})
export class DoctorAppointments implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  readonly canViewAgenda = this.authService.hasPermission('VER_CITAS');
  readonly canRegisterDiagnosis = this.authService.hasPermission('REGISTRAR_DIAGNOSTICO');

  private service = inject(DoctorService);

  appointments = signal<DoctorAppointment[]>([]);
  loading = signal(false);
  message = signal('');

  ngOnInit(): void {
    this.loadAgenda();
  }

  loadAgenda(): void {
    if (!this.canViewAgenda) {
      return;
    }
    this.loading.set(true);
    this.message.set('');
    this.service.getAgenda().subscribe({
      next: (appointments) => {
        this.appointments.set(appointments);
        this.loading.set(false);
      },
      error: (error) => {
        this.message.set(error.error?.message ?? 'No se pudo cargar la agenda.');
        this.loading.set(false);
      },
    });
  }
  appointmentId?: number;

  diagnosticoReceta = '';

  observaciones = '';

  saveDiagnosis() {
    if (!this.canRegisterDiagnosis || !this.appointmentId || !this.diagnosticoReceta.trim()) {
      return;
    }

    this.service
      .registerDiagnosis(this.appointmentId, this.diagnosticoReceta, this.observaciones)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          alert('Diagnóstico registrado correctamente');

          this.diagnosticoReceta = '';
          this.observaciones = '';
          this.loadAgenda();
        },

        error: (error) => {
          console.error(error);

          alert(error.error?.message ?? 'No se pudo registrar el diagnóstico');
        },
      });
  }
}
