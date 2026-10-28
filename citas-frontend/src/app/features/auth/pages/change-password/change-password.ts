import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [ReactiveFormsModule],
  templateUrl: './change-password.html',
})
export class ChangePassword {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);
  readonly saving = signal(false);
  readonly form = this.fb.nonNullable.group(
    {
      actual: ['', Validators.required],
      nueva: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(64)]],
      confirmacion: ['', Validators.required],
    },
    {
      validators: (group) =>
        group.get('nueva')?.value === group.get('confirmacion')?.value ? null : { different: true },
    },
  );

  save(): void {
    this.form.markAllAsTouched();
    if (this.form.invalid || this.saving()) return;
    this.saving.set(true);
    const { actual, nueva } = this.form.getRawValue();
    this.auth
      .changePassword(actual, nueva)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.form.reset();
          this.logout();
        },
        error: () => {},
      });
  }
  logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
