import {
  Component,
  inject,
  OnInit,
  signal
} from '@angular/core';

import {
  AppointmentService,
  Cita
} from '../appointment.service';

@Component({
  selector: 'app-patient-history',
  standalone: true,
  templateUrl: './patient-history.html'
})
export class PatientHistory
  implements OnInit {

  private service =
    inject(AppointmentService);

  appointments =
    signal<Cita[]>([]);

  ngOnInit(): void {
    this.load();
  }

  load() {

    this.service
      .getHistory()
      .subscribe({

        next: data => {

          this.appointments.set(data);
        },

        error: error => {

          console.error(
            'Error cargando historial',
            error
          );
        }

      });
  }

  cancel(idCita: number) {

    if (
      !confirm(
        '¿Cancelar esta cita?'
      )
    ) {
      return;
    }

    this.service
      .cancelAppointment(idCita)
      .subscribe(() => {

        this.load();
      });
  }
}