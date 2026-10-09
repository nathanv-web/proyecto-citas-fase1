import { Component, inject, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { User } from '../../models/user.model';
import { UserService } from '../../services/user.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css'
})
export class UserList implements OnInit {

  private readonly userService = inject(UserService);

  users: User[] = [];

  loading = false;
  error = '';
  success = '';

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.error = '';

    this.userService.getAll().subscribe({
      next: (users) => {
        this.users = users;
        this.loading = false;
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudieron cargar los usuarios.';

        this.loading = false;
      }
    });
  }

  deactivateUser(user: User): void {

    if (!user.activo) {
      return;
    }

    const confirmed = confirm(
      `¿Deseas desactivar al usuario ${user.nombre} ${user.apellido}?`
    );

    if (!confirmed) {
      return;
    }

    this.error = '';
    this.success = '';

    this.userService.deactivate(user.idUsuario).subscribe({
      next: () => {
        this.success = 'Usuario desactivado correctamente.';
        this.loadUsers();
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudo desactivar el usuario.';
      }
    });
  }
}
