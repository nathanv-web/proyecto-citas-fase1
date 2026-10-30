import { Routes } from '@angular/router';

import { Login } from './features/auth/pages/login/login';
import { AccessDenied } from './features/auth/pages/access-denied/access-denied';

import { authGuard } from './core/auth/guards/auth.guard';
import { panelGuard } from './core/auth/guards/panel.guard';
import { PanelSelector } from './features/panels/panel-selector';

import { DoctorDashboard } from './features/doctor/doctor-dashboard/doctor-dashboard';
import { DoctorSchedules } from './features/doctor/doctor-schedules/doctor-schedules';
import { DoctorAppointments } from './features/doctor/doctor-appointments/doctor-appointments';

import { PatientDashboard } from './features/patient/patient-dashboard/patient-dashboard';
import { PatientDoctors } from './features/patient/patient-doctors/patient-doctors';
import { PatientSchedules } from './features/patient/patient-schedules/patient-schedules';
import { PatientNewAppointment } from './features/patient/patient-new-appointment/patient-new-appointment';
import { PatientHistory } from './features/patient/patient-history/patient-history';

import { Welcome } from './features/public/welcome/welcome';

/** Las rutas conectan las pantallas pÃºblicas y privadas con los controles de sesiÃ³n y acceso a paneles. */
export const routes: Routes = [
  {
    path: 'change-password',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/auth/pages/change-password/change-password').then((m) => m.ChangePassword),
  },
  {
    path: 'forgot-password',
    loadComponent: () =>
      import('./features/auth/pages/forgot-password/forgot-password').then((m) => m.ForgotPassword),
  },
  {
    path: 'reset-password',
    loadComponent: () =>
      import('./features/auth/pages/reset-password/reset-password').then((m) => m.ResetPassword),
  },
  {
    path: 'verify-email',
    loadComponent: () =>
      import('./features/auth/pages/verify-email/verify-email').then((m) => m.VerifyEmail),
  },
  {
    path: 'login',
    component: Login,
  },
  {
    path: 'register',
    loadComponent: () =>
      import('./features/admin/users/pages/user-form/user-form').then((m) => m.UserForm),
    data: { publicRegistration: true },
  },
  {
    path: 'panels',
    canActivate: [authGuard],
    component: PanelSelector,
  },

  {
    path: 'access-denied',
    component: AccessDenied,
  },
  {
    path: 'admin',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'admin' },
    loadChildren: () => import('./features/admin/admin.routes').then((m) => m.ADMIN_ROUTES),
  },

  {
    path: 'doctor',
    pathMatch: 'full',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'doctor' },
    component: DoctorDashboard,
  },
  {
    path: 'doctor/doctors',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'doctor', functions: ['doctors'] },
    component: PatientDoctors,
  },
  {
    path: 'doctor/schedules',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'doctor', functions: ['schedules'] },
    component: DoctorSchedules,
  },
  {
    path: 'doctor/appointments',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'doctor', functions: ['appointments', 'diagnosis'] },
    component: DoctorAppointments,
  },

  {
    path: 'patient',
    pathMatch: 'full',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'patient' },
    component: PatientDashboard,
  },
  {
    path: 'patient/doctors',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'patient', functions: ['doctors'] },
    component: PatientDoctors,
  },
  {
    path: 'patient/schedules',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'patient', functions: ['schedules'] },
    component: PatientSchedules,
  },
  {
    path: 'patient/appointment/new',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'patient', functions: ['newAppointment'] },
    component: PatientNewAppointment,
  },
  {
    path: 'patient/history',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'patient', functions: ['history'] },
    component: PatientHistory,
  },

  {
    path: '',
    component: Welcome,
    pathMatch: 'full',
  },
  { path: '**', redirectTo: '' },
];
