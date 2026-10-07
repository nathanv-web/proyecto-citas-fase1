import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';
import { AlertController } from '@ionic/angular';

import { Auth } from '../../core/auth/services/auth';
import { RegisterRequest } from '../../core/auth/models/register-request';

@Component({
  selector: 'app-register',
  templateUrl: './register.page.html',
  styleUrls: ['./register.page.scss'],
  standalone: false,
})
export class RegisterPage {

  registerForm: FormGroup;
  loading = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private router: Router,
    private auth: Auth,
    private alertController: AlertController
  ) {
    this.registerForm = this.fb.group({
      nombre: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100)
      ]],
      apellido: ['', [
        Validators.required,
        Validators.minLength(2),
        Validators.maxLength(100)
      ]],
      correo: ['', [
        Validators.required,
        Validators.email
      ]],
      telefono: ['', [
        Validators.required,
        Validators.maxLength(20)
      ]],
      contrasena: ['', [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(12)
      ]]
    });
  }

  registrar(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    const data: RegisterRequest = this.registerForm.value;

    this.auth.register(data).subscribe({
      next: async () => {
        this.loading = false;

        const alert = await this.alertController.create({
          header: 'Cuenta creada',
          message: 'Tu cuenta fue creada correctamente. Ya puedes iniciar sesión.',
          buttons: ['Continuar']
        });

        await alert.present();
        await alert.onDidDismiss();

        this.router.navigateByUrl('/login', {
          replaceUrl: true
        });
      },

      error: (error: HttpErrorResponse) => {
        this.loading = false;

        if (error.status === 0) {
          this.errorMessage =
            'No se pudo conectar con el servidor.';
          return;
        }

        this.errorMessage =
          error.error?.message ||
          'No se pudo crear la cuenta.';
      }
    });
  }

  volver(): void {
    this.router.navigateByUrl('/welcome');
  }
}
