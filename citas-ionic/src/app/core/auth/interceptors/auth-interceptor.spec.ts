import { HttpHandler, HttpRequest, HttpResponse } from '@angular/common/http';
import { firstValueFrom, of } from 'rxjs';
import { API_BASE_URL } from '../../config/api.config';
import { Auth } from '../services/auth';
import { AuthInterceptor } from './auth-interceptor';

describe('AuthInterceptor', () => {
  it('restores the saved session before attaching a bearer token to API requests', async () => {
    const auth = {
      restoreSession: vi.fn().mockResolvedValue(true),
      getToken: vi.fn().mockReturnValue('saved-token'),
      logout: vi.fn(),
    };
    const router = { navigateByUrl: vi.fn() };
    const notifications = { error: vi.fn() };
    const interceptor = new AuthInterceptor(
      router as never,
      notifications as never,
      auth as unknown as Auth,
    );
    let forwarded: HttpRequest<unknown> | undefined;
    const next = {
      handle: vi.fn((request: HttpRequest<unknown>) => {
        forwarded = request;
        return of(new HttpResponse({ status: 200 }));
      }),
    } as unknown as HttpHandler;

    await firstValueFrom(interceptor.intercept(new HttpRequest('GET', `${API_BASE_URL}/medicos`), next));

    expect(auth.restoreSession).toHaveBeenCalledOnce();
    expect(forwarded?.headers.get('Authorization')).toBe('Bearer saved-token');
  });

  it('does not restore a session or attach credentials to public login requests', async () => {
    const auth = {
      restoreSession: vi.fn(),
      getToken: vi.fn().mockReturnValue('saved-token'),
      logout: vi.fn(),
    };
    const interceptor = new AuthInterceptor(
      { navigateByUrl: vi.fn() } as never,
      { error: vi.fn() } as never,
      auth as unknown as Auth,
    );
    let forwarded: HttpRequest<unknown> | undefined;
    const next = {
      handle: (request: HttpRequest<unknown>) => {
        forwarded = request;
        return of(new HttpResponse({ status: 200 }));
      },
    } as unknown as HttpHandler;

    await firstValueFrom(interceptor.intercept(new HttpRequest('POST', `${API_BASE_URL}/auth/login`, null), next));

    expect(auth.restoreSession).not.toHaveBeenCalled();
    expect(forwarded?.headers.has('Authorization')).toBe(false);
  });
});
