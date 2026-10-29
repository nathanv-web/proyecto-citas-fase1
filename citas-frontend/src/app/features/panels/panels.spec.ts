import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../app.routes';
import { AuthService } from '../../core/auth/services/auth.service';
import { of } from 'rxjs';

describe('Selección de paneles y funciones por permisos', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    const auth = TestBed.inject(AuthService);
    vi.spyOn(auth, 'refreshSession').mockImplementation(() =>
      of({
        usuario: auth.getUsuario()!,
        debeCambiarPassword: false,
      }),
    );
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  function authenticate(permisos: string[], roles = ['ROLE_PERSONALIZADO']) {
    localStorage.setItem('auth_token', 'test-token');
    localStorage.setItem(
      'auth_user',
      JSON.stringify({
        idUsuario: 1,
        nombre: 'Ana',
        apellido: 'Lopez',
        correo: 'ana@example.com',
        roles,
        permisos,
      }),
    );
  }

  function links(harness: RouterTestingHarness): string[] {
    return Array.from(harness.routeNativeElement?.querySelectorAll('a') ?? []).map(
      (link) => link.getAttribute('href') ?? '',
    );
  }

  it('explica que no hay funciones disponibles sin conceder acceso por ROLE_ADMIN', async () => {
    authenticate([], ['ROLE_ADMIN']);
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/panels');
    expect(harness.routeNativeElement?.textContent).toContain('No tienes funciones disponibles');
    expect(links(harness).some((url) => ['/admin', '/doctor', '/patient'].includes(url))).toBe(
      false,
    );
    await harness.navigateByUrl('/admin');
    expect(TestBed.inject(Router).url).toBe('/access-denied');
  });

  it('requiere sesión para abrir el selector', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/panels');
    expect(TestBed.inject(Router).url).toBe('/login');
  });

  it('requiere sesión para abrir un panel', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/admin');
    expect(TestBed.inject(Router).url).toBe('/login');
  });
});
