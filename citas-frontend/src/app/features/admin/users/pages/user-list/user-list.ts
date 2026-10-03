import { Component, OnInit, inject,ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { AuthService } from '../../../../../core/auth/services/auth.service';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css'
})
export class UserList implements OnInit {

  // =================================================
  // SERVICIOS
  // =================================================

  private usuarioService = inject(UserService);

  public authService = inject(AuthService);

  private cdr = inject(ChangeDetectorRef);


  // =================================================
  // VARIABLES
  // =================================================

  usuarios: User[] = [];

  cargando = false;

  error = '';


  // =================================================
  // INICIALIZACIÓN
  // =================================================

  ngOnInit(): void {
    if (this.authService.hasPermission('VER_USUARIOS')) {
      this.listarUsuarios();
    }
  }


  // =================================================
  // LISTAR USUARIOS
  // =================================================

  listarUsuarios(): void {

  this.cargando = true;
  this.error = '';

  this.usuarioService.listarUsuarios().subscribe({

    next: (usuarios) => {

      console.log('Usuarios recibidos:', usuarios.length);

      this.usuarios = usuarios;
      this.cargando = false;

      this.cdr.detectChanges();
    },

    error: (err) => {

      console.error('Error al listar usuarios:', err.status);

      this.cargando = false;

      this.error =
        err.error?.message ??
        'No se pudieron cargar los usuarios';

      this.cdr.detectChanges();
    }

  });
}


  // =================================================
  // DESACTIVAR USUARIO
  // =================================================

  desactivarUsuario(usuario: User): void {

    if (!this.authService.hasPermission('DESACTIVAR_USUARIOS')) {
      return;
    }

    if (!usuario.activo) {
      return;
    }

    const confirmar = window.confirm(
      `¿Deseas desactivar a ${usuario.nombre} ${usuario.apellido}?`
    );

    if (!confirmar) {
      return;
    }

    this.usuarioService
      .desactivarUsuario(usuario.idUsuario)
      .subscribe({

        next: () => {
          this.listarUsuarios();
        },

        error: (err) => {
          this.error =
            err.error?.message ??
            'No se pudo desactivar el usuario';
        }

      });
  }

}
