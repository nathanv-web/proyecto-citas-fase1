import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Role, RoleService } from '../role.service';

@Component({
  selector: 'app-role-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './role-list.html'
})
export class RoleList implements OnInit {

  private roleService = inject(RoleService);

  roles = signal<Role[]>([]);

  ngOnInit(): void {
    this.loadRoles();
  }

  loadRoles() {

    this.roleService
      .getRoles()
      .subscribe({
        next: data => {
          this.roles.set(data);
        },

        error: error => {
          console.error(
            'Error al cargar roles',
            error
          );
        }
      });
  }

  deleteRole(id?: number) {

    if (!id) {
      return;
    }

    if (!confirm('¿Eliminar este rol?')) {
      return;
    }

    this.roleService
      .deleteRole(id)
      .subscribe({
        next: () => {
          this.loadRoles();
        },

        error: error => {
          console.error(
            'Error al eliminar',
            error
          );
        }
      });
  }
}