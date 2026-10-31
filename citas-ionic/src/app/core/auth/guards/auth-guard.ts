import { Injectable } from '@angular/core';
import {
  ActivatedRouteSnapshot,
  CanActivate,
  Router,
  RouterStateSnapshot,
  UrlTree
} from '@angular/router';

import { Auth } from '../services/auth';

@Injectable({
  providedIn: 'root',
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: Auth,
    private router: Router
  ) {}

  canActivate(
    _route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Promise<boolean | UrlTree> {
    return this.authService
      .restoreSession()
      .then((restored) => {
        if (restored && this.authService.isAuthenticated()) {
          return true;
        }
        return this.loginUrl(state.url);
      })
      .catch(() => this.loginUrl(state.url));
  }

  private loginUrl(returnUrl: string): UrlTree {
    return this.router.createUrlTree(['/login'], { queryParams: { returnUrl } });
  }
}
