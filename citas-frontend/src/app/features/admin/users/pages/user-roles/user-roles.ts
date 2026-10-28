import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';
import { Role } from '../../../roles/models/role.model';
import { RoleService } from '../../../roles/services/role.service';

import { finalize } from 'rxjs';

/** El componente consulta los roles disponibles y guarda la selección asignada a un usuario. */
@Component({
  selector: 'app-user-roles',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-roles.html',
  styleUrl: './user-roles.css',
})
export class UserRoles implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);

  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  private readonly userService = inject(UserService);
  private readonly roleService = inject(RoleService);

  userId = 0;

  user: User | null = null;
  roles: Role[] = [];

  selectedRoleIds: number[] = [];

  loading = true;
  saving = false;
  error = '';
  success = '';

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (!id) {
      this.error = 'No se encontró el usuario.';
      this.loading = false;
      return;
    }

    this.userId = Number(id);

    this.loadData();
  }

  loadData(): void {
    this.loading = true;
    this.error = '';

    this.userService
      .getById(this.userId)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (user) => {
          this.user = user;

          this.roleService
            .getAll()
            .pipe(finalize(() => this.changeDetector.markForCheck()))
            .subscribe({
              next: (roles) => {
                this.roles = roles;

                this.selectedRoleIds = roles
                  .filter((role) => user.roles.includes(role.nombre))
                  .map((role) => role.idRol);

                this.loading = false;
              },

              error: (error) => {
                this.loading = false;
              },
            });
        },

        error: (error) => {
          this.loading = false;
        },
      });
  }

  isSelected(idRol: number): boolean {
    return this.selectedRoleIds.includes(idRol);
  }

  toggleRole(idRol: number): void {
    if (this.isSelected(idRol)) {
      this.selectedRoleIds = this.selectedRoleIds.filter((id) => id !== idRol);
    } else {
      this.selectedRoleIds = [...this.selectedRoleIds, idRol];
    }
  }

  saveRoles(): void {
    this.error = '';
    this.success = '';

    if (this.selectedRoleIds.length === 0) {
      this.error = 'Debe seleccionar al menos un rol.';
      return;
    }

    this.saving = true;

    this.userService
      .assignRoles(this.userId, {
        idRol: this.selectedRoleIds,
      })
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          this.success = 'Roles asignados correctamente.';

          this.saving = false;

          setTimeout(() => {
            this.router.navigate(['/admin/users']);
          }, 800);
        },

        error: (error) => {
          this.saving = false;
        },
      });
  }
}
