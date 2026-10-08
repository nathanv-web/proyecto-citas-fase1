import {
  Component,
  inject
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  DoctorService
} from '../doctor.service';

@Component({
  selector: 'app-doctor-schedules',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl:
    './doctor-schedules.html'
})
export class DoctorSchedules {

  private fb =
    inject(FormBuilder);

  private service =
    inject(DoctorService);

  message = '';

  form = this.fb.group({

    idMedico: [
      null,
      Validators.required
    ],

    fecha: [
      '',
      Validators.required
    ],

    horaInicio: [
      '',
      Validators.required
    ],

    horaFin: [
      '',
      Validators.required
    ]

  });

  save() {

    if (this.form.invalid) {
      return;
    }

    this.service
      .createSchedule(
        Number(
          this.form.value.idMedico
        ),
        this.form.value.fecha!,
        this.form.value.horaInicio!,
        this.form.value.horaFin!
      )
      .subscribe({

        next: () => {

          this.message =
            'Horario creado correctamente';

          this.form.reset();
        },

        error: error => {

          console.error(error);

          this.message =
            error.error?.message
            ?? 'No se pudo crear el horario';
        }

      });
  }
}