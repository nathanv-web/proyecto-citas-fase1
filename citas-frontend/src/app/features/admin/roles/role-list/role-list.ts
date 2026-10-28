import { ConfirmationService } from '../../../../core/notifications/confirmation.service';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Role } from '../models/role.model';
import { RoleService } from '../services/role.service';
import { AuthService } from '../../../../core/auth/services/auth.service';

import { finalize } from 'rxjs';

/** El listado presenta los roles con sus permisos y las acciones de administración disponibles. */
@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './role-list.html',
  styleUrl: './role-list.css',
})
export class RoleList implements OnInit {
  private readonly confirmation = inject(ConfirmationService);
  private readonly changeDetector = inject(ChangeDetectorRef);

  private readonly roleService = inject(RoleService);
  private readonly authService = inject(AuthService);

  readonly canCreateRole = this.authService.hasPermission('CREAR_ROLES');
  readonly canEditRole = this.authService.hasPermission('ACTUALIZAR_ROLES');
  readonly canDeleteRole = this.authService.hasPermission('ELIMINAR_ROLES');

  roles: Role[] = [];

  loading = false;
  error = '';
  success = '';

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(): void {
    this.loading = true;
    this.error = '';

    this.roleService
      .getAll()
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (roles) => {
          this.roles = roles;
          this.loading = false;
        },

        error: (error) => {
          this.loading = false;
        },
      });
  }

  async deleteRole(role: Role): Promise<void> {
    if (!this.canDeleteRole) {
      return;
    }

    const confirmed = await this.confirmation.confirm(`¿Deseas eliminar el rol ${role.nombre}?`);

    if (!confirmed) {
      return;
    }

    this.error = '';
    this.success = '';

    this.roleService
      .delete(role.idRol)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          this.success = 'Rol eliminado correctamente.';
          this.loadRoles();
        },

        error: (error) => {},
      });
  }
}
