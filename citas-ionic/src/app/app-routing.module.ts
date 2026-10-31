import { NgModule } from '@angular/core';
import { PreloadAllModules, RouterModule, Routes } from '@angular/router';

import { AuthGuard } from './core/auth/guards/auth-guard';

const routes: Routes = [
  {
  path: 'home',
  loadChildren: () => import('./home/home.module').then(m => m.HomePageModule),
  canActivate: [AuthGuard]
},
  {
  path: '',
  redirectTo: 'welcome',
  pathMatch: 'full'
  },
  {
    path: 'login',
    loadChildren: () => import('./pages/login/login.module').then( m => m.LoginPageModule)
  },
  {
  path: 'doctors',
  loadChildren: () => import('./pages/doctors/doctors.module').then(m => m.DoctorsPageModule),
  canActivate: [AuthGuard]
  },
  {
  path: 'specialties',
  loadChildren: () =>
    import('./pages/specialties/specialties.module')
      .then(m => m.SpecialtiesPageModule),
  canActivate: [AuthGuard]
  },
  {
  path: 'schedules',
  loadChildren: () => import('./pages/schedules/schedules.module').then(m => m.SchedulesPageModule),
  canActivate: [AuthGuard]
  },
  {
    path: 'welcome',
    loadChildren: () => import('./pages/welcome/welcome.module').then( m => m.WelcomePageModule)
  },
  {
    path: 'register',
    loadChildren: () => import('./pages/register/register.module').then( m => m.RegisterPageModule)
  },
  {
    path: 'confirm-appointment',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/appointments/pages/confirm-appointment/confirm-appointment.module').then( m => m.ConfirmAppointmentPageModule)
  },
  {
    path: 'my-appointments',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/appointments/pages/my-appointments/my-appointments.module').then( m => m.MyAppointmentsPageModule)
  },
  {
    path: 'appointment-detail',
    canActivate: [AuthGuard],
    loadChildren: () => import('./features/appointments/pages/appointment-detail/appointment-detail.module').then( m => m.AppointmentDetailPageModule)
  },

];

@NgModule({
  imports: [
    RouterModule.forRoot(routes, { preloadingStrategy: PreloadAllModules })
  ],
  exports: [RouterModule]
})
export class AppRoutingModule { }
