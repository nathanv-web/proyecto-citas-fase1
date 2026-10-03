import { Routes } from '@angular/router';

import { authGuard } from '../../../core/auth/guards/auth.guard';
import { permissionGuard } from '../../../core/auth/guards/permission.guard';
// =================================================
// RUTAS DE ADMINISTRACIÓN DE USUARIOS
// =================================================

export const USERS_ROUTES: Routes = [

  // =================================================
  // LISTAR USUARIOS
  // =================================================
  // Permiso: VER_USUARIOS

  {
    path: '',
    loadComponent: () =>
      import('./pages/user-list/user-list')
        .then(m => m.UserList),

    canActivate: [authGuard, permissionGuard],

    data: {
      permission: 'VER_USUARIOS'
    }
  },


  // =================================================
  // CREAR USUARIO
  // =================================================
  // Permiso: CREAR_USUARIOS

  {
    path: 'new',
    loadComponent: () =>
     import('./pages/user-form/user-form')
        .then(m => m.UserForm),

    canActivate: [authGuard, permissionGuard],

    data: {
      permission: 'CREAR_USUARIOS'
    }
  },


  // =================================================
  // EDITAR USUARIO
  // =================================================
  // Permiso: ACTUALIZAR_USUARIOS

  {
    path: 'edit/:id',
    loadComponent: () =>
      import('./pages/user-form/user-form')
        .then(m => m.UserForm),

    canActivate: [authGuard, permissionGuard],

    data: {
      permission: 'ACTUALIZAR_USUARIOS'
    }
  },


  // =================================================
  // ASIGNAR ROLES
  // =================================================
  // Permiso: ASIGNAR_ROLES

  {
    path: 'roles/:id',
    loadComponent: () =>
      import('./pages/user-roles/user-roles')
        .then(m => m.UserRoles),

    canActivate: [authGuard, permissionGuard],

    data: {
      permission: 'ASIGNAR_ROLES'
    }
  }

];
