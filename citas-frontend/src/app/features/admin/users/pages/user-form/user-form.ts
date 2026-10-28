import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

import { UserService } from '../../services/user.service';
import { UserRequest } from '../../models/user-request.model';
import { UserUpdate } from '../../models/user-update.model';
import { AuthService } from '../../../../../core/auth/services/auth.service';

import { finalize } from 'rxjs';

/** El formulario gestiona el registro público, la creación y la edición de usuarios según la ruta. */
@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './user-form.html',
  styleUrl: './user-form.css',
})
export class UserForm implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);

  private readonly userService = inject(UserService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  readonly isRegistration = this.route.snapshot.data['publicRegistration'] === true;
  readonly backLink = this.isRegistration
    ? '/login'
    : this.authService.hasPermission('VER_USUARIOS')
      ? '/admin/users'
      : '/admin';

  userId = 0;
  isEdit = false;

  loading = false;
  saving = false;
  error = '';
  success = '';

  form = {
    nombre: '',
    apellido: '',
    correo: '',
    telefono: '',
    contrasena: '',
    activo: true,
  };

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.userId = Number(id);
      this.isEdit = true;
      this.loadUser();
    }
  }

  loadUser(): void {
    this.loading = true;
    this.error = '';

    this.userService
      .getById(this.userId)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (user) => {
          this.form.nombre = user.nombre;
          this.form.apellido = user.apellido;
          this.form.correo = user.correo;
          this.form.telefono = user.telefono;
          this.form.activo = user.activo;

          this.loading = false;
        },

        error: (error) => {
          this.loading = false;
        },
      });
  }

  save(): void {
    this.error = '';
    this.success = '';

    if (
      !this.form.nombre.trim() ||
      !this.form.apellido.trim() ||
      !this.form.correo.trim() ||
      !this.form.telefono.trim()
    ) {
      this.error = 'Completa los campos obligatorios.';
      return;
    }

    this.saving = true;

    if (this.isEdit) {
      this.updateUser();
    } else {
      this.createUser();
    }
  }

  private createUser(): void {
    if (this.form.contrasena.length < 8 || this.form.contrasena.length > 64) {
      this.error = 'La contraseña debe tener entre 8 y 64 caracteres.';

      this.saving = false;
      return;
    }

    const request: UserRequest = {
      nombre: this.form.nombre.trim(),
      apellido: this.form.apellido.trim(),
      correo: this.form.correo.trim(),
      telefono: this.form.telefono.trim(),
      contrasena: this.form.contrasena,
    };

    const operation = this.isRegistration
      ? this.authService.register(request)
      : this.userService.create(request);

    operation.pipe(finalize(() => this.changeDetector.markForCheck())).subscribe({
      next: () => {
        this.success = 'Usuario creado correctamente.';
        this.saving = false;

        if (this.isRegistration) {
          this.router.navigate(['/login'], { queryParams: { registered: true } });
          return;
        }

        setTimeout(() => {
          this.router.navigate([
            this.authService.hasPermission('VER_USUARIOS') ? '/admin/users' : '/admin',
          ]);
        }, 800);
      },

      error: (error) => {
        this.saving = false;
      },
    });
  }

  private updateUser(): void {
    const request: UserUpdate = {
      nombre: this.form.nombre.trim(),
      apellido: this.form.apellido.trim(),
      correo: this.form.correo.trim(),
      telefono: this.form.telefono.trim(),
      activo: this.form.activo,
    };

    this.userService
      .update(this.userId, request)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          this.success = 'Usuario actualizado correctamente.';
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
