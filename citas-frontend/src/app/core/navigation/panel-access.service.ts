import { inject, Injectable } from '@angular/core';
import { AuthService } from '../auth/services/auth.service';
import { PANELS, PanelDefinition, PanelFunction, PanelId } from './panels.config';

// permite que el servicio se inyecte en otros componentes o servicios
@Injectable({ providedIn: 'root' })

// Inyecta el servicio AuthService para verificar los permisos del usuario y determinar si puede acceder a un panel o función específica.
export class PanelAccessService {
  private readonly authService = inject(AuthService);


  /*
  getDefinition(): obtiene la configuración de un panel mediante su identificador.
getFunctions(): devuelve las funcionalidades autorizadas según los permisos del usuario.
getPanels(): obtiene los paneles disponibles para el usuario.
canAccess(): comprueba si el usuario tiene acceso a un panel o a alguna de las funcionalidades indicadas.
  */
  getDefinition(panelId: PanelId): PanelDefinition | undefined {
    return PANELS.find(panel => panel.id === panelId);
  }

  getFunctions(panelId: PanelId): readonly PanelFunction[] {
    const permissions = this.authService.getUsuario()?.permisos ?? [];
    const panel = this.getDefinition(panelId);
    if (!panel || !permissions.includes(panel.accessPermission)) {
      return [];
    }
    return panel.functions.filter(feature =>
      feature.permissions.every(permission => permissions.includes(permission))
    );
  }

  getPanels(): readonly PanelDefinition[] {
    return PANELS.filter(panel => this.canAccess(panel.id));
  }

  canAccess(panelId: PanelId, functionIds?: readonly string[]): boolean {
    return this.getFunctions(panelId).some(feature => !functionIds || functionIds.includes(feature.id));
  }
}
