import { Routes } from '@angular/router';

import { AdminDashboard } from './admin-dashboard/admin-dashboard';

import {
  UserList
} from './users/pages/user-list/user-list';

import {
  UserForm
} from './users/pages/user-form/user-form';

import {
  UserRoles
} from './users/pages/user-roles/user-roles';

import {
  RoleList
} from './roles/role-list/role-list';

import {
  RoleForm
} from './roles/role-form/role-form';

import {
  SpecialtyList
} from './specialties/specialty-list/specialty-list';

import {
  DoctorList
} from './doctors/doctor-list/doctor-list';

export const ADMIN_ROUTES: Routes = [

  {
    path: '',
    component: AdminDashboard
  },

  {
    path: 'users',
    component: UserList
  },

  {
    path: 'users/new',
    component: UserForm
  },

  {
    path: 'users/edit/:id',
    component: UserForm
  },

  {
    path: 'users/roles/:id',
    component: UserRoles
  },

  {
    path: 'roles',
    component: RoleList
  },

  {
    path: 'roles/new',
    component: RoleForm
  },

  {
    path: 'roles/edit/:id',
    component: RoleForm
  },

  {
    path: 'specialties',
    component: SpecialtyList
  },

  {
    path: 'doctors',
    component: DoctorList
  }

];
