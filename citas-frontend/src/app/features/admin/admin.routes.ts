import { Routes } from '@angular/router';

import { AdminDashboard } from './admin-dashboard/admin-dashboard';
import { authGuard } from '../../core/auth/guards/auth.guard';
import { permissionGuard } from '../../core/auth/guards/permission.guard';
import { panelGuard } from '../../core/auth/guards/panel.guard';

import { UserList } from './users/pages/user-list/user-list';

import { UserForm } from './users/pages/user-form/user-form';

import { UserRoles } from './users/pages/user-roles/user-roles';

import { RoleList } from './roles/role-list/role-list';

import { RoleForm } from './roles/role-form/role-form';

import { SpecialtyList } from './specialties/specialty-list/specialty-list';

import { DoctorList } from './doctors/doctor-list/doctor-list';

/** Las rutas administrativas se cargan bajo /admin y conservan los permisos específicos de cada operación. */
export const ADMIN_ROUTES: Routes = [
  {
    path: 'statistics',
    canActivate: [authGuard, permissionGuard],
    data: {
      permissions: ['ACCESO_ADMIN', 'VER_USUARIOS', 'VER_MEDICOS', 'VER_ROLES', 'VER_CITAS'],
    },
    loadComponent: () => import('./statistics/admin-statistics').then((m) => m.AdminStatistics),
  },
  {
    path: '',
    pathMatch: 'full',
    canActivate: [authGuard],
    component: AdminDashboard,
  },

  {
    path: 'users',
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'VER_USUARIOS' },
    component: UserList,
  },

  {
    path: 'users/new',
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'CREAR_USUARIOS' },
    component: UserForm,
  },

  {
    path: 'users/edit/:id',
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'ACTUALIZAR_USUARIOS', permissions: ['VER_USUARIOS'] },
    component: UserForm,
  },

  {
    path: 'users/roles/:id',
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'ASIGNAR_ROLES', permissions: ['VER_USUARIOS', 'VER_ROLES'] },
    component: UserRoles,
  },

  {
    path: 'roles',
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'VER_ROLES' },
    component: RoleList,
  },

  {
    path: 'roles/new',
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'CREAR_ROLES' },
    component: RoleForm,
  },

  {
    path: 'roles/edit/:id',
    canActivate: [authGuard, permissionGuard],
    data: { permission: 'ACTUALIZAR_ROLES', permissions: ['VER_ROLES'] },
    component: RoleForm,
  },

  {
    path: 'specialties',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'admin', functions: ['specialties', 'newSpecialty'] },
    component: SpecialtyList,
  },

  {
    path: 'doctors',
    canActivate: [authGuard, panelGuard],
    data: { panel: 'admin', functions: ['doctors', 'newDoctor'] },
    component: DoctorList,
  },
];
