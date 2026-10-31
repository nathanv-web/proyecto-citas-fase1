import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { AuthService } from './auth.service';

describe('AuthService password reset session handling', () => {
  let auth: AuthService;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [provideHttpClient(), provideHttpClientTesting()] });
    auth = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('clears the current browser session after a successful password reset', () => {
    localStorage.setItem('auth_token', 'old-session-token');
    localStorage.setItem('auth_user', JSON.stringify({ correo: 'person@example.com' }));
    localStorage.setItem('auth_password_change', 'true');

    auth.resetPassword('a'.repeat(43), 'NuevaClave123').subscribe();
    http.expectOne('/api/v1/auth/password/reset').flush({ message: 'Contraseña actualizada' });

    expect(auth.isAuthenticated()).toBe(false);
    expect(localStorage.getItem('auth_user')).toBeNull();
    expect(localStorage.getItem('auth_password_change')).toBeNull();
  });

  it('keeps the current session if the reset request fails', () => {
    localStorage.setItem('auth_token', 'still-valid-session-token');
    auth.resetPassword('a'.repeat(43), 'NuevaClave123').subscribe({ error: () => {} });
    http.expectOne('/api/v1/auth/password/reset').flush(
      { message: 'El enlace expiró' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(auth.getToken()).toBe('still-valid-session-token');
  });
});
