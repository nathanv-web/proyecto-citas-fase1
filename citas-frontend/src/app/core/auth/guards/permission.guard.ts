import { catchError, map, of } from 'rxjs';
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

  return authService.refreshSession().pipe(
    map((session) => {
      if (session.debeCambiarPassword) return router.createUrlTree(['/change-password']);
      return (
        requiredPermissions.every((required) => authService.hasPermission(required)) ||
        router.createUrlTree(['/access-denied'])
      );
    }),
    catchError(() => of(false)),
  );
};
