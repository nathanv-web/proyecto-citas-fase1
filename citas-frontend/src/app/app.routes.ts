import { Routes } from '@angular/router';

import { Login } from './features/auth/pages/login/login';
import { AccessDenied } from './features/auth/pages/access-denied/access-denied';

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


  // =================================================
  // RUTA INICIAL
  // =================================================

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  }

];