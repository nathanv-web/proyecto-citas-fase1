import { Routes } from '@angular/router';
import { RolesComponent } from './features/roles/roles';

export const routes: Routes = [
  { path: '', redirectTo: 'roles', pathMatch: 'full' },
  { path: 'roles', component: RolesComponent }
];