import {
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { filter, finalize, Subscription } from 'rxjs';
import { AuthService } from '../../core/auth/services/auth.service';
import { LoginUsuario } from '../../core/auth/models/login-response.model';
import { ProfilePhotoService } from '../../services/profile-photo.service';
import { apiErrorMessage } from '../../core/notifications/api-error';

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
    '(window:storage)': 'sessionChanged($event)',
  },
})
export class UserProfile {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly photos = inject(ProfilePhotoService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private photoRequest?: Subscription;

  protected usuario = this.authService.getUsuario();
  protected readonly isOpen = signal(false);
  protected readonly photoUrl = signal<string | null>(null);
  protected readonly photoLoading = signal(false);
  protected readonly photoSaving = signal(false);
  protected readonly photoError = signal('');
  protected readonly photoMessage = signal('');

  protected get fullName(): string {
    return (
      [this.usuario?.nombre, this.usuario?.apellido].filter(Boolean).join(' ').trim() || 'Mi cuenta'
    );
  }
  protected get accountType(): string {
    return (
      this.usuario?.roles
        ?.map((role) => {
          const labels: Record<string, string> = {
            ROLE_ADMIN: 'Administrador',
            ROLE_DOCTOR: 'Médico',
            ROLE_PATIENT: 'Paciente',
          };
          return labels[role] ?? role.replace(/^ROLE_/, '').replace(/_/g, ' ');
        })
        .join(', ') || 'Usuario'
    );
  }

  constructor() {
    this.router.events
      .pipe(
        filter((event) => event instanceof NavigationEnd),
        takeUntilDestroyed(),
      )
      .subscribe(() => this.closeProfile());
    this.destroyRef.onDestroy(() => {
      this.photoRequest?.unsubscribe();
      const url = this.photoUrl();
      if (url) URL.revokeObjectURL(url);
    });
    if (this.usuario?.idUsuario && !this.authService.mustChangePassword()) this.loadPhoto();
  }

  protected toggleProfile(): void {
    this.updateUser(this.authService.getUsuario());
    this.isOpen.update((open) => !open);
    if (this.isOpen()) {
      this.authService
        .refreshSession()
        .pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe({
          next: (session) => {
            this.updateUser(session.usuario);
            this.changeDetector.markForCheck();
          },
          error: () => {},
        });
    }
  }

  private updateUser(user: LoginUsuario | null): boolean {
    const changed = this.usuario?.idUsuario !== user?.idUsuario;
    if (changed) {
      this.photoRequest?.unsubscribe();
      this.replacePhoto(null);
      this.photoLoading.set(false);
      this.photoSaving.set(false);
      this.photoError.set('');
      this.photoMessage.set('');
    }
    this.usuario = user;
    if (changed && user?.idUsuario && !this.authService.mustChangePassword()) this.loadPhoto();
    return changed;
  }

  protected sessionChanged(event: StorageEvent): void {
    if (event.key === 'auth_user' || event.key === null) {
      const changed = this.updateUser(this.authService.getUsuario());
      this.closeProfile();
      this.changeDetector.markForCheck();
      if (changed) {
        void this.router.navigateByUrl(this.usuario ? '/panels' : '/login', { replaceUrl: true });
      }
    }
  }

  private belongsToCurrentUser(id: number): boolean {
    return this.usuario?.idUsuario === id && this.authService.getUsuario()?.idUsuario === id;
  }

  private loadPhoto(): void {
    const id = this.usuario?.idUsuario;
    if (!id) return;
    this.photoLoading.set(true);
    this.photoRequest = this.photos
      .getPhoto()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.photoLoading.set(false)),
      )
      .subscribe({
        next: (photo) => {
          if (this.belongsToCurrentUser(id)) this.replacePhoto(photo);
        },
        error: (error) => {
          if (this.belongsToCurrentUser(id)) this.photoError.set(apiErrorMessage(error));
        },
      });
  }

  private replacePhoto(photo: Blob | null): void {
    const previous = this.photoUrl();
    this.photoUrl.set(photo?.size ? URL.createObjectURL(photo) : null);
    if (previous) URL.revokeObjectURL(previous);
  }

  protected selectPhoto(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (this.updateUser(this.authService.getUsuario())) return;
    if (!file || this.photoSaving() || this.photoLoading()) return;
    const id = this.usuario?.idUsuario;
    if (!id) return;
    this.photoError.set('');
    this.photoMessage.set('');
    if (!['image/jpeg', 'image/png'].includes(file.type) || file.size > 1024 * 1024) {
      this.photoError.set('Selecciona una foto JPG o PNG de hasta 1 MiB.');
      return;
    }
    this.photoSaving.set(true);
    this.photoRequest = this.photos
      .savePhoto(file)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.photoSaving.set(false)),
      )
      .subscribe({
        next: () => {
          if (!this.belongsToCurrentUser(id)) return;
          this.photoMessage.set('Foto guardada en tu cuenta.');
          this.loadPhoto();
        },
        error: (error) => {
          if (this.belongsToCurrentUser(id)) this.photoError.set(apiErrorMessage(error));
        },
      });
  }

  protected removePhoto(): void {
    if (this.updateUser(this.authService.getUsuario())) return;
    if (this.photoSaving() || this.photoLoading()) return;
    const id = this.usuario?.idUsuario;
    if (!id) return;
    this.photoError.set('');
    this.photoMessage.set('');
    this.photoSaving.set(true);
    this.photoRequest = this.photos
      .removePhoto()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.photoSaving.set(false)),
      )
      .subscribe({
        next: () => {
          if (!this.belongsToCurrentUser(id)) return;
          this.replacePhoto(null);
          this.photoMessage.set('Foto eliminada.');
        },
        error: (error) => {
          if (this.belongsToCurrentUser(id)) this.photoError.set(apiErrorMessage(error));
        },
      });
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
