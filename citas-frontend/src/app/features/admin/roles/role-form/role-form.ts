import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { RoleService } from '../services/role.service';
import {
  RoleRequest,
  RoleUpdate
} from '../models/role.model';

@Component({
  selector: 'app-role-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './role-form.html',
  styleUrl: './role-form.css'
})
export class RoleForm implements OnInit {

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
    descripcion: ''
  };

  ngOnInit(): void {

    const id =
      this.route.snapshot.paramMap.get('id');

    if (id) {
      this.roleId = Number(id);
      this.isEdit = true;
    }

    this.loadPermissions();

    if (this.isEdit) {
      this.loadRole();
    }
  }

  loadPermissions(): void {

    this.roleService.getPermissions().subscribe({
      next: (permissions) => {
        this.permissions = permissions;
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudieron cargar los permisos.';
      }
    });
  }

  loadRole(): void {

    this.loading = true;
    this.error = '';

    this.roleService.getById(this.roleId).subscribe({
      next: (role) => {

        this.form.nombre = role.nombre;
        this.form.descripcion =
          role.descripcion || '';

        this.selectedPermissions =
          [...(role.permisos || [])];

        this.loading = false;
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudo cargar el rol.';

        this.loading = false;
      }
    });
  }

  isPermissionSelected(permission: string): boolean {
    return this.selectedPermissions.includes(permission);
  }

  togglePermission(permission: string): void {

    if (this.isPermissionSelected(permission)) {

      this.selectedPermissions =
        this.selectedPermissions.filter(
          item => item !== permission
        );

    } else {

      this.selectedPermissions = [
        ...this.selectedPermissions,
        permission
      ];
    }
  }

  save(): void {

    this.error = '';
    this.success = '';

    const nombre =
      this.form.nombre.trim().toUpperCase();

    if (!nombre) {
      this.error =
        'El nombre del rol es obligatorio.';
      return;
    }

    if (!/^ROLE_[A-Z][A-Z0-9_]*$/.test(nombre)) {
      this.error =
        'El rol debe utilizar el formato ROLE_NOMBRE.';
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
      descripcion: this.form.descripcion.trim()
    };

    this.roleService.create(request).subscribe({
      next: (role) => {

        this.roleService.assignPermissions(
          role.idRol,
          this.selectedPermissions
        ).subscribe({
          next: () => {

            this.success =
              'Rol creado correctamente.';

            this.saving = false;

            setTimeout(() => {
              this.router.navigate(['/admin/roles']);
            }, 800);
          },

          error: (error) => {
            this.error =
              error?.error?.message ||
              'El rol fue creado, pero no se pudieron asignar los permisos.';

            this.saving = false;
          }
        });
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudo crear el rol.';

        this.saving = false;
      }
    });
  }

  private updateRole(): void {

    const request: RoleUpdate = {
      nombre: this.form.nombre,
      descripcion: this.form.descripcion.trim()
    };

    this.roleService.update(
      this.roleId,
      request
    ).subscribe({
      next: () => {

        this.roleService.assignPermissions(
          this.roleId,
          this.selectedPermissions
        ).subscribe({
          next: () => {

            this.success =
              'Rol y permisos actualizados correctamente.';

            this.saving = false;

            setTimeout(() => {
              this.router.navigate(['/admin/roles']);
            }, 800);
          },

          error: (error) => {
            this.error =
              error?.error?.message ||
              'El rol fue actualizado, pero no se pudieron guardar los permisos.';

            this.saving = false;
          }
        });
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudo actualizar el rol.';

        this.saving = false;
      }
    });
  }
}
