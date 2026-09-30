import { Routes } from '@angular/router';

import { Login } from './features/auth/login/login';
import { AccessDenied } from './features/auth/access-denied/access-denied';

export const routes: Routes = [
  {
    path: 'login',
    component: Login
  },
  {
    path: 'access-denied',
    component: AccessDenied
  },
  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }
];