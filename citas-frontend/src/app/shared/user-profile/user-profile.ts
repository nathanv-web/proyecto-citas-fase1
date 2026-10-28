import { Component, ElementRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter } from 'rxjs';
import { AuthService } from '../../core/auth/services/auth.service';

/** Presenta los datos personales de la cuenta y permite cerrar su sesión. */
@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './user-profile.html',
  styleUrl: './user-profile.css',
  host: {
    '(document:click)': 'closeOnOutsideClick($event)',
    '(document:keydown.escape)': 'closeOnEscape($event)',
    '(focusout)': 'closeOnFocusOut($event)',
  },
})
export class UserProfile {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  protected readonly usuario = this.authService.getUsuario();
  protected readonly isOpen = signal(false);
  protected readonly fullName =
    [this.usuario?.nombre, this.usuario?.apellido].filter(Boolean).join(' ').trim() || 'Mi cuenta';
  protected readonly accountType =
    this.usuario?.roles
      ?.map((role) => {
        const labels: Record<string, string> = {
          ROLE_ADMIN: 'Administrador',
          ROLE_DOCTOR: 'Médico',
          ROLE_PATIENT: 'Paciente',
        };
        return labels[role] ?? role.replace(/^ROLE_/, '').replace(/_/g, ' ');
      })
      .join(', ') || 'Usuario';

  constructor() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.closeProfile());
  }

  protected toggleProfile(): void {
    this.isOpen.update((open) => !open);
  }

  protected closeProfile(restoreFocus = false): void {
    this.isOpen.set(false);
    if (restoreFocus) {
      this.host.nativeElement.querySelector<HTMLButtonElement>('.profile-trigger')?.focus();
    }
  }

  protected closeOnOutsideClick(event: MouseEvent): void {
    if (this.isOpen() && !this.host.nativeElement.contains(event.target as Node)) {
      this.closeProfile();
    }
  }

  protected closeOnEscape(event: Event): void {
    if (this.isOpen()) {
      event.preventDefault();
      this.closeProfile(true);
    }
  }

  protected closeOnFocusOut(event: FocusEvent): void {
    if (
      this.isOpen() &&
      event.relatedTarget &&
      !this.host.nativeElement.contains(event.relatedTarget as Node)
    ) {
      this.closeProfile();
    }
  }

  protected logout(): void {
    this.authService.logout();
    this.closeProfile();
    void this.router.navigateByUrl('/', { replaceUrl: true });
  }
}
