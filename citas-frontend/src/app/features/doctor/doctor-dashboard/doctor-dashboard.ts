import {
  Component
} from '@angular/core';

import {
  RouterLink
} from '@angular/router';

@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [RouterLink],
  template: `
    <h1>Panel del Doctor</h1>

    <a routerLink="/doctor/schedules">
      Crear horario
    </a>

    <br><br>

    <a routerLink="/doctor/appointments">
      Ver citas
    </a>
  `
})
export class DoctorDashboard {}