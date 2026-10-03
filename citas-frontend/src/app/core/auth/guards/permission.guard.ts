import { inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

export const permissionGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const permission = route.data['permission'] as string | undefined;

  if (!permission) {
    return true;
  }

  if (authService.hasPermission(permission)) {
    return true;
  }

  return router.createUrlTree(['/access-denied']);
};
