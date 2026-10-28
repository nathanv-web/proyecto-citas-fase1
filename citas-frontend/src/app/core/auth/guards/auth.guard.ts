import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (_route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.isAuthenticated()) return router.createUrlTree(['/login']);
  return auth.refreshSession().pipe(
    map((session) =>
      session.debeCambiarPassword && state.url !== '/change-password'
        ? router.createUrlTree(['/change-password'])
        : true,
    ),
    catchError(() => of(false)),
  );
};
