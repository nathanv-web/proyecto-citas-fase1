import { ChangeDetectorRef, Component } from '@angular/core';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AppointmentService } from '../../services/appointment.service';
import { AppointmentResponse } from '../../models/appointment-response';

@Component({
  selector: 'app-my-appointments',
  templateUrl: './my-appointments.page.html',
  styleUrls: ['./my-appointments.page.scss'],
  standalone: false,
})
export class MyAppointmentsPage {

  citas: AppointmentResponse[] = [];

  loading = false;
  errorMessage = '';

  constructor(
    private appointmentService: AppointmentService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ionViewWillEnter(): void {
    this.cargarCitas();
  }

  cargarCitas(): void {

    this.loading = true;
    this.errorMessage = '';

    this.appointmentService
      .obtenerHistorial()
      .subscribe({

        next: (citas) => {
          this.citas = citas;
          this.loading = false;

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {
          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No fue posible cargar tus citas.';

          this.cdr.markForCheck();
        }

      });
  }

  verDetalle(cita: AppointmentResponse): void {

    this.router.navigate(
      ['/appointment-detail'],
      {
        queryParams: {
          idCita: cita.idCita
        }
      }
    );
  }

}
