import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { PanelAccessService } from '../../navigation/panel-access.service';
import { PanelId } from '../../navigation/panels.config';

/** El guard verifica el acceso al panel y a cualquiera de las funciones solicitadas por la ruta. */
export const panelGuard: CanActivateFn = (route) => {
  const access = inject(PanelAccessService);
  const router = inject(Router);
  return (
    access.canAccess(route.data['panel'] as PanelId, route.data['functions']) ||
    router.createUrlTree(['/access-denied'])
  );
};
