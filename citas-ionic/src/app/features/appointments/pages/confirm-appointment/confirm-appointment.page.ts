import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AppointmentService } from '../../services/appointment.service';
import { AppointmentResponse } from '../../models/appointment-response';

@Component({
  selector: 'app-confirm-appointment',
  templateUrl: './confirm-appointment.page.html',
  styleUrls: ['./confirm-appointment.page.scss'],
  standalone: false,
})
export class ConfirmAppointmentPage implements OnInit {

  idHorario: number | null = null;

  medico = '';
  fecha = '';
  horaInicio = '';
  horaFin = '';

  motivo = '';

  loading = false;
  errorMessage = '';

  citaCreada: AppointmentResponse | null = null;

  constructor(
    private route: ActivatedRoute,
    private appointmentService: AppointmentService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {

    const idHorario =
      this.route.snapshot.queryParamMap.get('idHorario');

    this.medico =
      this.route.snapshot.queryParamMap.get('medico') ?? '';

    this.fecha =
      this.route.snapshot.queryParamMap.get('fecha') ?? '';

    this.horaInicio =
      this.route.snapshot.queryParamMap.get('horaInicio') ?? '';

    this.horaFin =
      this.route.snapshot.queryParamMap.get('horaFin') ?? '';

    if (idHorario) {
      this.idHorario = Number(idHorario);
    }
  }

  confirmarCita(): void {

    this.errorMessage = '';

    if (this.idHorario === null) {
      this.errorMessage =
        'No se encontró el horario seleccionado.';

      this.cdr.markForCheck();
      return;
    }

    if (!this.motivo.trim()) {
      this.errorMessage =
        'Ingresá el motivo de la consulta.';

      this.cdr.markForCheck();
      return;
    }

    this.loading = true;
    this.cdr.markForCheck();

    this.appointmentService
      .crearCita({
        idHorario: this.idHorario,
        motivo: this.motivo.trim()
      })
      .subscribe({

        next: (cita) => {
          this.citaCreada = cita;
          this.loading = false;

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {
          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No fue posible reservar la cita. Intentá nuevamente.';

          this.cdr.markForCheck();
        }

      });
  }
}
