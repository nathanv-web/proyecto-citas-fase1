import { ChangeDetectorRef, Component, inject } from '@angular/core';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

import { AppointmentService } from '../appointment.service';

import { finalize } from 'rxjs';

/** El formulario reserva una cita con el identificador del horario y el motivo de consulta. */
@Component({
  selector: 'app-patient-new-appointment',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './patient-new-appointment.html',
})
export class PatientNewAppointment {
  private readonly changeDetector = inject(ChangeDetectorRef);

  private fb = inject(FormBuilder);

  private service = inject(AppointmentService);

  message = '';

  form = this.fb.group({
    idHorario: [null, Validators.required],

    motivo: ['', [Validators.required, Validators.minLength(5)]],
  });

  save() {
    if (this.form.invalid) {
      this.message = 'Complete correctamente los campos';

      return;
    }

    const idHorario = Number(this.form.value.idHorario);

    const motivo = this.form.value.motivo!;

    this.service
      .createAppointment(idHorario, motivo)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          this.message = 'Cita creada correctamente';

          this.form.reset();
        },

        error: (error) => {
          console.error(error);

          this.message = error.error?.message ?? 'No se pudo crear la cita';
        },
      });
  }
}
