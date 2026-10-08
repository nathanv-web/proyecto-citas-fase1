import { Component, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';

import { AuthService } from '../../../../core/auth/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private readonly formBuilder = inject(FormBuilder);
  private readonly authService = inject(AuthService);

  loading = signal(false);
  errorMessage = signal('');
  successMessage = signal('');

  loginForm = this.formBuilder.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required]
  });

  iniciarSesion(): void {
    this.errorMessage.set('');
    this.successMessage.set('');

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading.set(true);

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Inicio de sesión exitoso.');
      },
      error: (error: HttpErrorResponse) => {
        this.loading.set(false);

        switch (error.status) {
          case 400:
            this.errorMessage.set('Los datos enviados no son válidos.');
            break;
          case 401:
            this.errorMessage.set('Correo o contraseña incorrectos.');
            break;
          case 403:
            this.errorMessage.set('No tienes autorización para acceder.');
            break;
          case 404:
            this.errorMessage.set('No se encontró el servicio solicitado.');
            break;
          case 409:
            this.errorMessage.set('Existe un conflicto con los datos enviados.');
            break;
          default:
            this.errorMessage.set('No fue posible iniciar sesión. Intenta nuevamente.');
        }
      }
    });
  }
}
