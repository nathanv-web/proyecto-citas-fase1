import { Component, computed, inject, signal } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterOutlet } from '@angular/router';
import { toSignal } from '@angular/core/rxjs-interop';
import { filter, map } from 'rxjs';
import { AuthService } from './core/auth/services/auth.service';
import { UserProfile } from './shared/user-profile/user-profile';

/** El componente principal presenta las rutas y la navegación de las pantallas privadas. */
@Component({
  imports: [RouterOutlet, RouterLink, UserProfile],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  // La navegación se actualiza al terminar cada cambio de ruta.
  private readonly currentUrl = toSignal(
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
      map((event) => event.urlAfterRedirects),
    ),
    { initialValue: this.router.url },
  );

  protected readonly showPanelNavigation = computed(() => {
    const path = this.currentUrl().split(/[?#]/)[0];
    return !['/', '/login', '/register'].includes(path) && this.authService.isAuthenticated();
  });

  protected readonly title = signal('citas-fronted');
}
