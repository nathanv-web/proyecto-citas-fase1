import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing'; // para simular peticiones HTTP en pruebas unitarias y no levantar spring boot
import { provideRouter, Router } from '@angular/router';
import { authInterceptor } from './auth.interceptor';
import { AccessDenied } from '../../../features/auth/pages/access-denied/access-denied';

describe('Autenticación de las peticiones', () => {
  let client: HttpClient;
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({ providers: [
      provideHttpClient(withInterceptors([authInterceptor])), provideHttpClientTesting(),
      provideRouter([
        { path: 'login', component: AccessDenied },
        { path: 'access-denied', component: AccessDenied },
      ]),
    ] });
    client = TestBed.inject(HttpClient);
    http = TestBed.inject(HttpTestingController);
    localStorage.setItem('auth_token', 'cached-token');
  });

  afterEach(() => { http.verify(); localStorage.clear(); });

  it('permite iniciar sesión aunque exista un token antiguo', () => {
    client.post('/api/v1/auth/login', {}).subscribe();
    const request = http.expectOne('/api/v1/auth/login');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('borra la sesión y vuelve al login cuando la API devuelve 401', async () => {
    client.get('/api/v1/users').subscribe({ error: () => {} });
    http.expectOne('/api/v1/users').flush({}, { status: 401, statusText: 'Unauthorized' });
    await new Promise(resolve => setTimeout(resolve, 0));
    expect(localStorage.getItem('auth_token')).toBeNull();
    expect(TestBed.inject(Router).url).toBe('/login');
  });
});
