import { ConfirmationService } from '../../../../../core/notifications/confirmation.service';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { apiErrorMessage } from '../../../../../core/notifications/api-error';

import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { AuthService } from '../../../../../core/auth/services/auth.service';

import { finalize, Observable } from 'rxjs';

/** El listado consulta los usuarios y muestra las acciones permitidas para cada registro. */
@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [RouterLink, FormsModule],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
})
export class UserList implements OnInit {
  private readonly confirmation = inject(ConfirmationService);
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
  searchTerm = '';
  statusFilter = '';

  loading = false;
  error = '';
  success = '';
  changingState = false;

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    if (this.loading || this.changingState) return;
    this.loading = true;
    this.error = '';

    this.userService
      .getAll({
        search: this.searchTerm,
        activo: this.statusFilter === '' ? undefined : this.statusFilter === 'true',
      })
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (users) => {
          this.users = users;
          this.loading = false;
        },

        error: (error) => {
          this.error = apiErrorMessage(error);
          this.loading = false;
        },
      });
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.statusFilter = '';
    this.loadUsers();
  }

  async changeUserState(user: User): Promise<void> {
    const activate = !user.activo;
    if (
      this.loading ||
      this.changingState ||
      !(activate ? this.canActivateUser : this.canDeactivateUser)
    ) {
      return;
    }

    this.changingState = true;
    const confirmed = await this.confirmation.confirm(
      `¿Deseas ${activate ? 'activar' : 'desactivar'} al usuario ${user.nombre} ${user.apellido}?`,
    );

    if (!confirmed) {
      this.changingState = false;
      this.changeDetector.markForCheck();
      return;
    }

    this.error = '';
    this.success = '';

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
          this.changingState = false;
          this.loadUsers();
        },

        error: (error) => {
          this.error = apiErrorMessage(error);
        },
      });
  }
}
