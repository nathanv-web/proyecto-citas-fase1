import { ActivatedRouteSnapshot, Router, RouterStateSnapshot } from '@angular/router';
import { Auth } from '../services/auth';
import { AuthGuard } from './auth-guard';

describe('AuthGuard', () => {
  it('waits for the stored session before allowing a private route', async () => {
    const auth = { restoreSession: vi.fn().mockResolvedValue(true), isAuthenticated: () => true };
    const router = { createUrlTree: vi.fn() };
    const guard = new AuthGuard(auth as unknown as Auth, router as unknown as Router);

    const result = await guard.canActivate(
      {} as ActivatedRouteSnapshot,
      { url: '/home' } as RouterStateSnapshot,
    );
    expect(result).toBe(true);
    expect(auth.restoreSession).toHaveBeenCalledOnce();
  });

  it('redirects to login when session restoration finds no saved credentials', async () => {
    const tree = { login: true };
    const auth = { restoreSession: vi.fn().mockResolvedValue(false), isAuthenticated: () => false };
    const router = { createUrlTree: vi.fn().mockReturnValue(tree) };
    const guard = new AuthGuard(auth as unknown as Auth, router as unknown as Router);

    const result = await guard.canActivate(
      {} as ActivatedRouteSnapshot,
      { url: '/appointments' } as RouterStateSnapshot,
    );
    expect(result).toBe(tree);
    expect(auth.restoreSession).toHaveBeenCalledOnce();
    expect(router.createUrlTree).toHaveBeenCalledWith(['/login'], {
      queryParams: { returnUrl: '/appointments' },
    });
  });
});
