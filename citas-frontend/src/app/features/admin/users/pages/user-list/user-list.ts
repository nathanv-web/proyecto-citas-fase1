import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../../../../core/auth/services/auth.service';

import { finalize, Observable } from 'rxjs';

/** El listado consulta los usuarios y muestra las acciones permitidas para cada registro. */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
})
export class UserList implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);

  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);

  readonly canCreateUser = this.authService.hasPermission('CREAR_USUARIOS');
  readonly canEditUser = this.authService.hasPermission('ACTUALIZAR_USUARIOS');
  readonly canAssignRoles =
    this.authService.hasPermission('ASIGNAR_ROLES') && this.authService.hasPermission('VER_ROLES');
  readonly canDeactivateUser = this.authService.hasPermission('DESACTIVAR_USUARIOS');
  readonly canActivateUser = this.authService.hasPermission('ACTUALIZAR_USUARIOS');

  users: User[] = [];

  loading = false;
  error = '';
  success = '';
  changingState = false;

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';

    this.userService
      .getAll()
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (users) => {
          this.users = users;
          this.loading = false;
        },

        error: (error) => {
          this.error = error?.error?.message || 'No se pudieron cargar los usuarios.';

          this.loading = false;
        },
      });
  }

  changeUserState(user: User): void {
    const activate = !user.activo;
    if (this.changingState || !(activate ? this.canActivateUser : this.canDeactivateUser)) {
      return;
    }

    const confirmed = confirm(
      `¿Deseas ${activate ? 'activar' : 'desactivar'} al usuario ${user.nombre} ${user.apellido}?`,
    );

    if (!confirmed) {
      return;
    }

    this.error = '';
    this.success = '';
    this.changingState = true;

    const request: Observable<unknown> = activate
      ? this.userService.activate(user.idUsuario)
      : this.userService.deactivate(user.idUsuario);
    request
      .pipe(
        finalize(() => {
          this.changingState = false;
          this.changeDetector.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.success = `Usuario ${activate ? 'activado' : 'desactivado'} correctamente.`;
          this.loadUsers();
        },

        error: (error) => {
          this.error =
            error?.error?.message ||
            `No se pudo ${activate ? 'activar' : 'desactivar'} el usuario.`;
        },
      });
  }
}
