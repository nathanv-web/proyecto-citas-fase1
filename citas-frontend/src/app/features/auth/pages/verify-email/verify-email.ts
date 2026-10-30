import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './verify-email.html',
  styleUrl: '../login/login.css',
})
export class VerifyEmail {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  readonly validToken = /^[A-Za-z0-9_-]{43}$/.test(this.token);

  readonly verifying = signal(false);
  readonly requesting = signal(false);
  readonly verified = signal(false);
  readonly requestSent = signal(false);
  readonly errorMessage = signal('');

  readonly form = this.fb.nonNullable.group({
    email: ['', [
      Validators.required,
      Validators.email,
      Validators.maxLength(255)
    ]]
  });

  verificar(): void {
    if (!this.validToken || this.verifying() || this.verified()) {
      return;
    }

    this.errorMessage.set('');
    this.verifying.set(true);

    this.auth.verifyEmail(this.token)
      .pipe(finalize(() => this.verifying.set(false)))
      .subscribe({
        next: () => {
          this.verified.set(true);
        },
        error: () => {
          this.errorMessage.set(
            'No se pudo verificar el correo. Revisa el aviso de error.'
          );
        }
      });
  }

  solicitar(): void {
    this.form.markAllAsTouched();

    if (this.form.invalid || this.requesting() || this.requestSent()) {
      return;
    }

    this.errorMessage.set('');
    this.requesting.set(true);

    const email = this.form.controls.email.value.trim();

    this.auth.requestEmailVerification(email)
      .pipe(finalize(() => this.requesting.set(false)))
      .subscribe({
        next: () => {
          this.requestSent.set(true);
        },
        error: () => {
          this.errorMessage.set(
            'No se pudo procesar la solicitud. Revisa el aviso de error.'
          );
        }
      });
  }
}