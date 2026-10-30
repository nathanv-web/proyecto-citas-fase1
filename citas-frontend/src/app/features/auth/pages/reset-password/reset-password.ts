import { Component, inject, signal } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: '../login/login.css',
})
export class ResetPassword {

  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly route = inject(ActivatedRoute);

  readonly saving = signal(false);
  readonly completed = signal(false);

  readonly token = this.route.snapshot.queryParamMap.get('token') ?? '';
  readonly validToken = /^[A-Za-z0-9_-]{43}$/.test(this.token);

  readonly form = this.fb.nonNullable.group({
    nueva: [
      '',
      [
        Validators.required,
        Validators.minLength(8),
        Validators.maxLength(64)
      ]
    ],
    confirmacion: ['', Validators.required]
  }, {
    validators: (group: AbstractControl) =>
      group.get('nueva')?.value === group.get('confirmacion')?.value
        ? null
        : { notMatch: true }
  });

  guardar(): void {

    this.form.markAllAsTouched();

    if (
      this.form.invalid ||
      this.saving() ||
      this.completed() ||
      !this.validToken
    ) {
      return;
    }

    this.saving.set(true);

    const { nueva } = this.form.getRawValue();

    this.auth.resetPassword(this.token, nueva)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.completed.set(true);
          this.form.reset();
        },
        error: () => {
          // El interceptor global muestra el error.
        }
      });
  }
}