import { Component, OnInit, inject,ChangeDetectorRef  } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';

import { UserService } from '../../services/user.service';
import { RoleService } from '../../services/role.service';

import { User } from '../../models/user.model';
import { Role } from '../../models/role.model';



@Component({
  selector: 'app-user-roles',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-roles.html',
  styleUrl: './user-roles.css'
})
export class UserRoles implements OnInit {

  private usuarioService = inject(UserService);
  private rolService = inject(RoleService);
  private cdr = inject(ChangeDetectorRef);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  usuario: User | null = null;
  roles: Role[] = [];

  rolesSeleccionados: number[] = [];

  cargando = false;
  guardando = false;

  error = '';

  // ==============================================
  // CARGAR DATOS
  // ==============================================

  ngOnInit(): void {

    const id = Number(
      this.route.snapshot.paramMap.get('id')
    );

    if (!Number.isSafeInteger(id) || id <= 0) {
      this.error = 'El ID del usuario no es válido.';
      return;
    }

    this.cargarDatos(id);
  }

cargarDatos(id: number): void {

  this.cargando = true;
  this.error = '';

  forkJoin({
    usuario: this.usuarioService.buscarUsuario(id),
    roles: this.rolService.listarRoles()
  }).subscribe({

    next: ({ usuario, roles }) => {

      this.usuario = usuario;
      this.roles = roles;

      // Identificar los roles actuales del usuario.
      this.rolesSeleccionados = roles
        .filter(rol =>
          usuario.roles.includes(rol.nombre)
        )
        .map(rol => rol.idRol);

      this.cargando = false;

      // Actualizar la interfaz.
      this.cdr.detectChanges();
    },

    error: err => {

      this.error =
        typeof err.error?.message === 'string'
          ? err.error.message
          : 'No se pudieron cargar los roles.';

      this.cargando = false;

      // Mostrar el error si la petición falla.
      this.cdr.detectChanges();
    }

  });
}
  // ==============================================
  // SELECCIONAR O QUITAR ROL
  // ==============================================

  cambiarRol(idRol: number, seleccionado: boolean): void {

    if (seleccionado) {

      if (!this.rolesSeleccionados.includes(idRol)) {
        this.rolesSeleccionados = [
          ...this.rolesSeleccionados,
          idRol
        ];
      }

    } else {

      this.rolesSeleccionados =
        this.rolesSeleccionados.filter(
          id => id !== idRol
        );
    }
  }

  // ==============================================
  // GUARDAR ROLES
  // ==============================================

  guardarRoles(): void {

    if (
      !this.usuario ||
      this.guardando ||
      this.cargando ||
      this.rolesSeleccionados.length === 0
    ) {
      return;
    }

    // Evitamos enviar una selección incompleta si
    // los nombres existentes no aparecen en el
    // catálogo de roles.

    const nombresDisponibles =
      new Set(this.roles.map(rol => rol.nombre));

    const algunRolDesconocido =
      this.usuario.roles.some(
        nombre => !nombresDisponibles.has(nombre)
      );

    if (algunRolDesconocido) {
      this.error =
        'Hay roles actuales que no aparecen en el catálogo.';
      return;
    }

    this.guardando = true;
    this.error = '';

    this.usuarioService.asignarRol(
      this.usuario.idUsuario,
      {
        idRol: this.rolesSeleccionados
      }
    ).subscribe({

      next: () => {
        this.guardando = false;
        this.router.navigate(['/admin/users']);
      },

     error: err => {

  this.guardando = false;

  this.error =
    typeof err.error?.message === 'string'
      ? err.error.message
      : 'No se pudieron asignar los roles.';

  this.cdr.detectChanges();
}

    });
  }

}