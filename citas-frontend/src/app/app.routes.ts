import { Routes } from '@angular/router';

import { DoctorDashboard } from './features/doctor/doctor-dashboard/doctor-dashboard';
import { Schedules } from './features/doctor/schedules/schedules';
import { Appointments } from './features/doctor/appointments/appointments';
import { Diagnosis } from './features/doctor/diagnosis/diagnosis';

export const routes: Routes = [
  {
    path: 'doctor',
    children: [
      {
        path: '',
        component: DoctorDashboard
      },
      {
        path: 'schedules',
        component: Schedules
      },
      {
        path: 'appointments',
        component: Appointments
      },
      {
        path: 'diagnosis',
        component: Diagnosis
      },
      {
        path: 'diagnosis/:id',
        component: Diagnosis
      }
    ]
  },
  {
    path: '',
    redirectTo: 'doctor',
    pathMatch: 'full'
  }
];
