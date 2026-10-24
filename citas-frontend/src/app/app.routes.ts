import { Routes } from '@angular/router';
import { UsuariosComponent } from './pages/usuarios/usuarios.component';
import { RolesComponent } from './pages/roles/roles.component';
import { EstadisticasComponent } from './pages/estadisticas/estadisticas.component';

export const routes: Routes = [
  { path: 'usuarios', component: UsuariosComponent },
  { path: 'roles', component: RolesComponent },
  { path: 'estadisticas', component: EstadisticasComponent },
  { path: '', redirectTo: 'usuarios', pathMatch: 'full' },
  { path: '**', redirectTo: 'usuarios' }
];