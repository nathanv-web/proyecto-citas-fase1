import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/** El guard exige todos los permisos declarados por la ruta y dirige a acceso denegado si falta alguno. */
export const permissionGuard: CanActivateFn = (route: ActivatedRouteSnapshot) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const permission = route.data['permission'] as string | undefined;
  const permissions = route.data['permissions'] as string[] | undefined;
  const requiredPermissions = [...(permissions ?? []), ...(permission ? [permission] : [])];

  if (requiredPermissions.length === 0) {
    return true;
  }

  if (requiredPermissions.every((required) => authService.hasPermission(required))) {
    return true;
  }

  return router.createUrlTree(['/access-denied']);
};
