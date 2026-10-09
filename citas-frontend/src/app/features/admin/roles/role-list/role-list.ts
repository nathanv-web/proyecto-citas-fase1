import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Role } from '../models/role.model';
import { RoleService } from '../services/role.service';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './role-list.html',
  styleUrl: './role-list.css'
})
export class RoleList implements OnInit {

  private readonly roleService = inject(RoleService);

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

    this.roleService.getAll().subscribe({
      next: (roles) => {
        this.roles = roles;
        this.loading = false;
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudieron cargar los roles.';

        this.loading = false;
      }
    });
  }

  deleteRole(role: Role): void {

    const confirmed = confirm(
      `¿Deseas eliminar el rol ${role.nombre}?`
    );

    if (!confirmed) {
      return;
    }

    this.error = '';
    this.success = '';

    this.roleService.delete(role.idRol).subscribe({
      next: () => {
        this.success = 'Rol eliminado correctamente.';
        this.loadRoles();
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudo eliminar el rol.';
      }
    });
  }
}
