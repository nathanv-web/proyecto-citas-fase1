import { ConfirmationService } from '../../../../core/notifications/confirmation.service';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { apiErrorMessage } from '../../../../core/notifications/api-error';

import { Role } from '../models/role.model';
import { RoleService } from '../services/role.service';
import { AuthService } from '../../../../core/auth/services/auth.service';

import { finalize } from 'rxjs';

/** El listado presenta los roles con sus permisos y las acciones de administración disponibles. */
@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [RouterLink, FormsModule],
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
  searchTerm = '';
  private appliedSearch = '';
  page = 0;
  totalPages = 0;
  totalElements = 0;
  deleting = false;

  loading = false;
  error = '';
  success = '';

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles(page = this.page): void {
    if (this.loading || page < 0) return;
    this.loading = true;
    this.error = '';

    this.roleService
      .getPage(this.appliedSearch, page)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (result) => {
          this.roles = result.content;
          this.page = result.number;
          this.totalPages = result.totalPages;
          this.totalElements = result.totalElements;
          this.loading = false;
          if (this.roles.length === 0 && this.page > 0) this.loadRoles(this.page - 1);
        },

        error: (error) => {
          this.error = apiErrorMessage(error);
          this.loading = false;
        },
      });
  }

  searchRoles(): void {
    this.appliedSearch = this.searchTerm.trim();
    this.loadRoles(0);
  }

  clearFilters(): void {
    this.searchTerm = '';
    this.searchRoles();
  }

  async deleteRole(role: Role): Promise<void> {
    if (!this.canDeleteRole || this.deleting || this.loading) {
      return;
    }

    const confirmed = await this.confirmation.confirm(`¿Deseas eliminar el rol ${role.nombre}?`);

    if (!confirmed) {
      return;
    }

    this.error = '';
    this.success = '';
    this.deleting = true;

    this.roleService
      .delete(role.idRol)
      .pipe(
        finalize(() => {
          this.deleting = false;
          this.changeDetector.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.success = 'Rol eliminado correctamente.';
          this.loadRoles();
        },

        error: (error) => {
          this.error = apiErrorMessage(error);
        },
      });
  }
}
