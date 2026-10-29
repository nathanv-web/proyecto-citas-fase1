import { ChangeDetectorRef, Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AlertController } from '@ionic/angular';
import { HttpErrorResponse } from '@angular/common/http';

import { AppointmentService } from '../../services/appointment.service';
import { AppointmentResponse } from '../../models/appointment-response';

@Component({
  selector: 'app-appointment-detail',
  templateUrl: './appointment-detail.page.html',
  styleUrls: ['./appointment-detail.page.scss'],
  standalone: false,
})
export class AppointmentDetailPage {

  idCita: number | null = null;

  cita: AppointmentResponse | null = null;

  loading = false;
  cancelling = false;

  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private appointmentService: AppointmentService,
    private alertController: AlertController,
    private cdr: ChangeDetectorRef
  ) {}

  ionViewWillEnter(): void {

    const id =
      this.route.snapshot.queryParamMap.get('idCita');

    if (!id) {
      this.errorMessage =
        'No se encontró la cita seleccionada.';

      this.cdr.markForCheck();
      return;
    }

    this.idCita = Number(id);

    this.cargarDetalle();
  }

  cargarDetalle(): void {

    if (this.idCita === null) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.appointmentService
      .obtenerHistorial()
      .subscribe({

        next: (citas) => {

          this.cita =
            citas.find(
              cita => cita.idCita === this.idCita
            ) ?? null;

          this.loading = false;

          if (!this.cita) {
            this.errorMessage =
              'No fue posible encontrar la cita.';
          }

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {

          this.loading = false;

          this.errorMessage =
            error.error?.message ??
            'No fue posible cargar el detalle de la cita.';

          this.cdr.markForCheck();
        }

      });
  }

  puedeCancelar(): boolean {

    return (
      this.cita !== null &&
      this.cita.estado !== 'CANCELADA' &&
      this.cita.estado !== 'COMPLETADA'
    );
  }

  async solicitarCancelacion(): Promise<void> {

    if (!this.cita || !this.puedeCancelar()) {
      return;
    }

    const alert =
      await this.alertController.create({

        header: 'Cancelar cita',

        message:
          'Indicá brevemente el motivo de la cancelación.',

        inputs: [
          {
            name: 'motivo',
            type: 'textarea',
            placeholder: 'Motivo de cancelación'
          }
        ],

        buttons: [

          {
            text: 'Volver',
            role: 'cancel'
          },

          {
            text: 'Cancelar cita',
            role: 'destructive',

            handler: (data) => {

              const motivo =
                data.motivo?.trim();

              if (!motivo) {
                return false;
              }

              this.cancelarCita(motivo);

              return true;
            }
          }

        ]

      });

    await alert.present();
  }

  cancelarCita(
    motivoCancelacion: string
  ): void {

    if (!this.cita) {
      return;
    }

    this.cancelling = true;
    this.errorMessage = '';

    this.appointmentService
      .cancelarCita(
        this.cita.idCita,
        motivoCancelacion
      )
      .subscribe({

        next: (citaActualizada) => {

          this.cita = citaActualizada;
          this.cancelling = false;

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {

          this.cancelling = false;

          this.errorMessage =
            error.error?.message ??
            'No fue posible cancelar la cita.';

          this.cdr.markForCheck();
        }

      });
  }

}
