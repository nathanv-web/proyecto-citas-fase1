import { Routes } from '@angular/router';

import { Login } from './features/auth/pages/login/login';
import { AccessDenied } from './features/auth/pages/access-denied/access-denied';

import { RoleList } from './features/admin/roles/role-list/role-list';
import { RoleForm } from './features/admin/roles/role-form/role-form';

import { DoctorDashboard } from './features/doctor/doctor-dashboard/doctor-dashboard';
import { DoctorSchedules } from './features/doctor/doctor-schedules/doctor-schedules';
import { DoctorAppointments } from './features/doctor/doctor-appointments/doctor-appointments';

import { PatientDashboard } from './features/patient/patient-dashboard/patient-dashboard';
import { PatientDoctors } from './features/patient/patient-doctors/patient-doctors';
import { PatientSchedules } from './features/patient/patient-schedules/patient-schedules';
import { PatientNewAppointment } from './features/patient/patient-new-appointment/patient-new-appointment';
import { PatientHistory } from './features/patient/patient-history/patient-history';

import { Welcome } from './features/public/welcome/welcome';

export const routes: Routes = [

  // =================================================
  // AUTENTICACIÓN
  // =================================================

  {
    path: 'login',
    component: Login
  },

  // =================================================
  // ACCESO DENEGADO
  // =================================================

  {
    path: 'access-denied',
    component: AccessDenied
  },


  // =================================================
  // ADMINISTRACIÓN DE USUARIOS
  // =================================================
  // Módulo cargado mediante Lazy Loading.
  //
  // Los permisos de cada acción se validan dentro de users.routes.ts.

  {
    path: 'admin/users',
    loadChildren: () =>
      import('./features/admin/users/users.routes')
        .then(m => m.USERS_ROUTES)
  },

  {
  path: 'admin/roles',
  component: RoleList
},

{
  path: 'admin/roles/new',
  component: RoleForm
},

{
  path: 'admin/roles/edit/:id',
  component: RoleForm
},

// DOCTOR

{
  path: 'doctor',
  component: DoctorDashboard
},

{
  path: 'doctor/schedules',
  component: DoctorSchedules
},

{
  path: 'doctor/appointments',
  component: DoctorAppointments
},

// PATIENT

{
  path: 'patient',
  component: PatientDashboard
},

{
  path: 'patient/doctors',
  component: PatientDoctors
},

{
  path: 'patient/schedules',
  component: PatientSchedules
},

{
  path: 'patient/appointment/new',
  component: PatientNewAppointment
},

{
  path: 'patient/history',
  component: PatientHistory
},


  // =================================================
  // RUTA INICIAL
  // =================================================

  {
    path: '',
    component: Welcome,
    pathMatch: 'full'
  }

];