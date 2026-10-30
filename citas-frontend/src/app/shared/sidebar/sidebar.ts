import { Component, computed, effect, inject, input, signal } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { PanelAccessService } from '../../core/navigation/panel-access.service';
import { PanelFunction, PanelId } from '../../core/navigation/panels.config';
import { NotificationBellComponent } from '../notification-bell/notification-bell';

/** El menú lateral permite explorar paneles y abrir únicamente las acciones autorizadas. */
@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink , NotificationBellComponent],
  templateUrl: './sidebar.html',
  styleUrl: './sidebar.css',
})
export class Sidebar {
  private readonly access = inject(PanelAccessService);
  private readonly router = inject(Router);

  readonly currentUrl = input.required<string>();
  readonly expandedPanel = signal<PanelId | null>(null);
  readonly mobileOpen = signal(false);

  // La navegación termina después de que los guards actualizan los permisos de la sesión.
  readonly panels = computed(() => {
    this.currentUrl();
    return this.access.getPanels().map((panel) => ({
      ...panel,
      actions: this.access.getFunctions(panel.id),
    }));
  });

  readonly currentPath = computed(() => this.currentUrl().split(/[?#]/)[0]);
  readonly currentPanel = computed(() =>
    this.panels().find(
      (panel) =>
        this.currentPath() === panel.route || this.currentPath().startsWith(`${panel.route}/`),
    ),
  );

  readonly currentAction = computed(() => {
    const url = this.router.parseUrl(this.currentUrl());
    const actions = this.currentPanel()?.actions.filter(
      (action) =>
        action.route === this.currentPath() && (action.fragment ?? '') === (url.fragment ?? ''),
    );
    // Algunas pantallas permiten listar y crear; la URL conserva la acción elegida al recargar.
    return actions?.find((action) => action.id === url.queryParams['action']) ?? actions?.[0];
  });

  constructor() {
    effect(() => {
      const panel = this.currentPanel();
      this.expandedPanel.set(panel?.id ?? null);
      this.mobileOpen.set(false);
    });
  }

  togglePanel(panel: PanelId): void {
    this.expandedPanel.update((current) => (current === panel ? null : panel));
  }

  toggleMobileMenu(): void {
    this.mobileOpen.update((open) => !open);
  }

  closeMobileMenu(): void {
    this.mobileOpen.set(false);
  }

  isCurrentAction(panel: PanelId, action: PanelFunction): boolean {
    return this.currentPanel()?.id === panel && this.currentAction()?.id === action.id;
  }
}
