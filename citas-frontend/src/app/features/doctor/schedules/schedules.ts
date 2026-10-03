import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Schedule } from '../../../models/schedule.model';
import { ScheduleService } from '../../../services/schedule';

@Component({
  selector: 'app-schedules',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './schedules.html',
  styleUrl: './schedules.css'
})
export class Schedules {

  private readonly scheduleService = inject(ScheduleService);

  schedule: Schedule = {
    idMedico: 0,
    fecha: '',
    horaInicio: '',
    horaFin: ''
  };

  mensaje = '';
  error = '';
  guardando = false;

  crearHorario(): void {
    this.mensaje = '';
    this.error = '';

    if (
      !this.schedule.idMedico ||
      !this.schedule.fecha ||
      !this.schedule.horaInicio ||
      !this.schedule.horaFin
    ) {
      this.error = 'Completa todos los campos.';
      return;
    }

    if (this.schedule.horaInicio >= this.schedule.horaFin) {
      this.error = 'La hora de inicio debe ser menor que la hora de fin.';
      return;
    }

    this.guardando = true;

    this.scheduleService.createSchedule(this.schedule).subscribe({
      next: () => {
        this.mensaje = 'Horario creado correctamente.';

        this.schedule = {
          idMedico: 0,
          fecha: '',
          horaInicio: '',
          horaFin: ''
        };

        this.guardando = false;
      },
      error: (error) => {
        console.error(error);
        this.error =
          error?.error?.message ||
          'No se pudo crear el horario.';

        this.guardando = false;
      }
    });
  }
}
