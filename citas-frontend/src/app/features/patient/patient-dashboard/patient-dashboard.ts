import {
  Component
} from '@angular/core';

import {
  RouterLink
} from '@angular/router';

@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <h1>Panel del Paciente</h1>

    <a routerLink="/patient/doctors">
      Médicos
    </a>

    <br><br>

    <a routerLink="/patient/schedules">
      Horarios disponibles
    </a>

    <br><br>

    <a routerLink="/patient/appointment/new">
      Crear cita
    </a>

    <br><br>

    <a routerLink="/patient/history">
      Historial de citas
    </a>
  `
})
export class PatientDashboard {}