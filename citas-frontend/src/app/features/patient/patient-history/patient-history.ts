import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { AppointmentService, Cita, Horario } from '../appointment.service';
import { AuthService } from '../../../core/auth/services/auth.service';
import { NotificationService } from '../../../core/notifications/notification.service';
import { ConfirmationService } from '../../../core/notifications/confirmation.service';

@Component({
  selector: 'app-patient-history',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './patient-history.html',
})
export class PatientHistory implements OnInit {
  private readonly service = inject(AppointmentService);
  private readonly auth = inject(AuthService);
  private readonly notifications = inject(NotificationService);
  private readonly confirmation = inject(ConfirmationService);
  readonly canCancelAppointment = this.auth.hasPermission('CANCELAR_CITA');
  readonly canRescheduleAppointment = this.auth.hasPermission('CREAR_CITA');
  readonly appointments = signal<Cita[]>([]);
  readonly loading = signal(false);
  readonly busy = signal(false);
  readonly schedules = signal<Horario[]>([]);
  readonly selectedDate = signal(new Date().toLocaleDateString('en-CA'));
  readonly period = signal('todos');
  readonly visibleAppointments = computed(() => {
    if (this.period() === 'todos') return this.appointments();
    const start = new Date(this.selectedDate() + 'T00:00:00');
    const end = new Date(start);
    if (this.period() === 'semana') end.setDate(end.getDate() + 6);
    return this.appointments().filter((cita) => {
      const date = new Date(cita.fecha + 'T00:00:00');
      return date >= start && date <= end;
    });
  });
  selected?: Cita;
  idMedico = 0;
  fecha = '';
  idHorarioNuevo = 0;
  motivoCancelacion = '';

  ngOnInit(): void {
    this.load();
  }
  load(): void {
    this.loading.set(true);
    this.service
      .getHistory()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (data) => this.appointments.set(data),
        error: () => this.appointments.set([]),
      });
  }
  async cancel(idCita: number): Promise<void> {
    if (!this.canCancelAppointment || this.busy()) return;
    if (!(await this.confirmation.confirm('¿Cancelar esta cita? Se liberará el horario.'))) return;
    this.busy.set(true);
    this.service
      .cancelAppointment(idCita, this.motivoCancelacion)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Cita cancelada correctamente');
          this.motivoCancelacion = '';
          this.load();
        },
        error: () => {},
      });
  }
  openReschedule(cita: Cita): void {
    if (!this.canRescheduleAppointment) return;
    this.selected = cita;
    this.idMedico = cita.idMedico;
    this.fecha = '';
    this.idHorarioNuevo = 0;
    this.schedules.set([]);
  }
  clearSchedules(): void {
    this.idHorarioNuevo = 0;
    this.schedules.set([]);
  }
  searchSchedules(): void {
    this.clearSchedules();
    if (!this.idMedico || !this.fecha) return;
    const medicoConsultado = this.idMedico;
    const fechaConsultada = this.fecha;
    this.service.getAvailableSchedules(medicoConsultado, fechaConsultada).subscribe({
      next: (data) => {
        if (this.idMedico === medicoConsultado && this.fecha === fechaConsultada)
          this.schedules.set(data);
      },
      error: () => {},
    });
  }
  async reschedule(): Promise<void> {
    if (!this.selected || !this.idHorarioNuevo || this.busy() || !this.canRescheduleAppointment)
      return;
    if (!(await this.confirmation.confirm('¿Guardar el nuevo horario de esta cita?'))) return;
    const idCita = this.selected.idCita;
    const idHorario = this.idHorarioNuevo;
    this.busy.set(true);
    // La API valida la disponibilidad de forma atómica al guardar, incluso si otro paciente se adelanta.
    this.service
      .rescheduleAppointment(idCita, idHorario)
      .pipe(finalize(() => this.busy.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Cita reprogramada correctamente');
          this.selected = undefined;
          this.load();
        },
        error: (error) => {
          if (error.status === 409) this.searchSchedules();
        },
      });
  }
}
