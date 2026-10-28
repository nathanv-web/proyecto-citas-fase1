import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { RoleService } from '../services/role.service';
import { RoleRequest, RoleUpdate } from '../models/role.model';

import { finalize } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';

/** El formulario crea o actualiza roles y permite asignar permisos cuando la cuenta está autorizada. */
@Component({
  selector: 'app-role-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './role-form.html',
  styleUrl: './role-form.css',
})
export class RoleForm implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  readonly canAssignPermissions = this.authService.hasPermission('ASIGNAR_PERMISOS');
  readonly backLink = this.authService.hasPermission('VER_ROLES') ? '/admin/roles' : '/admin';

  private readonly roleService = inject(RoleService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  roleId = 0;
  isEdit = false;

  permissions: string[] = [];
  selectedPermissions: string[] = [];

  loading = false;
  saving = false;

  error = '';
  success = '';

  form = {
    nombre: '',
    descripcion: '',
  };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.roleId = Number(id);
      this.isEdit = true;
    }

    if (this.canAssignPermissions) {
      this.loadPermissions();
    }

    if (this.isEdit) {
      this.loadRole();
    }
  }

  loadPermissions(): void {
    this.roleService
      .getPermissions()
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (permissions) => {
          this.permissions = permissions;
        },

        error: (error) => {},
      });
  }

  loadRole(): void {
    this.loading = true;
    this.error = '';

    this.roleService
      .getById(this.roleId)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (role) => {
          this.form.nombre = role.nombre;
          this.form.descripcion = role.descripcion || '';

          this.selectedPermissions = [...(role.permisos || [])];

          this.loading = false;
        },

        error: (error) => {
          this.loading = false;
        },
      });
  }

  isPermissionSelected(permission: string): boolean {
    return this.selectedPermissions.includes(permission);
  }

  togglePermission(permission: string): void {
    if (this.isPermissionSelected(permission)) {
      this.selectedPermissions = this.selectedPermissions.filter((item) => item !== permission);
    } else {
      this.selectedPermissions = [...this.selectedPermissions, permission];
    }
  }

  save(): void {
    this.error = '';
    this.success = '';

    const nombre = this.form.nombre.trim().toUpperCase();

    if (!nombre) {
      this.error = 'El nombre del rol es obligatorio.';
      return;
    }

    if (!/^ROLE_[A-Z][A-Z0-9_]*$/.test(nombre)) {
      this.error = 'El rol debe utilizar el formato ROLE_NOMBRE.';
      return;
    }

    this.form.nombre = nombre;
    this.saving = true;

    if (this.isEdit) {
      this.updateRole();
    } else {
      this.createRole();
    }
  }

  private createRole(): void {
    const request: RoleRequest = {
      nombre: this.form.nombre,
      descripcion: this.form.descripcion.trim(),
    };

    this.roleService
      .create(request)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (role) => {
          if (!this.canAssignPermissions) {
            this.finishSave('Rol creado correctamente.');
            return;
          }

          this.roleService
            .assignPermissions(role.idRol, this.selectedPermissions)
            .pipe(finalize(() => this.changeDetector.markForCheck()))
            .subscribe({
              next: () => {
                this.finishSave('Rol creado correctamente.');
              },

              error: (error) => {
                this.saving = false;
              },
            });
        },

        error: (error) => {
          this.saving = false;
        },
      });
  }

  private updateRole(): void {
    const request: RoleUpdate = {
      nombre: this.form.nombre,
      descripcion: this.form.descripcion.trim(),
    };

    this.roleService
      .update(this.roleId, request)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          if (!this.canAssignPermissions) {
            this.finishSave('Rol actualizado correctamente.');
            return;
          }

          this.roleService
            .assignPermissions(this.roleId, this.selectedPermissions)
            .pipe(finalize(() => this.changeDetector.markForCheck()))
            .subscribe({
              next: () => {
                this.finishSave('Rol y permisos actualizados correctamente.');
              },

              error: (error) => {
                this.saving = false;
              },
            });
        },

        error: (error) => {
          this.saving = false;
        },
      });
  }

  private finishSave(message: string): void {
    this.success = message;
    this.saving = false;
    setTimeout(() => {
      this.router.navigate([
        this.authService.hasPermission('VER_ROLES') ? '/admin/roles' : '/admin',
      ]);
    }, 800);
  }
}
