import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AdminStatistics } from './admin-statistics';
import {
  ActivatedRouteSnapshot,
  provideRouter,
  Router,
  RouterStateSnapshot,
} from '@angular/router';
import { firstValueFrom, Observable } from 'rxjs';
import { permissionGuard } from '../../../core/auth/guards/permission.guard';
import { ADMIN_ROUTES } from '../admin.routes';

describe('Estadísticas administrativas reales', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });
  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('la ruta verifica permisos actuales del servidor aunque la sesión local tenga todos', async () => {
    const required = ['ACCESO_ADMIN', 'VER_USUARIOS', 'VER_MEDICOS', 'VER_ROLES', 'VER_CITAS'];
    localStorage.setItem('auth_user', JSON.stringify({ permisos: required }));
    const route = {
      data: ADMIN_ROUTES.find((route) => route.path === 'statistics')!.data,
    } as ActivatedRouteSnapshot;
    const decision = firstValueFrom(
      TestBed.runInInjectionContext(() =>
        permissionGuard(route, {} as RouterStateSnapshot),
      ) as Observable<unknown>,
    );
    http.expectOne('/api/v1/me').flush({
      usuario: { idUsuario: 1, permisos: ['ACCESO_ADMIN', 'VER_USUARIOS'] },
      debeCambiarPassword: false,
    });
    expect(String(await decision)).toBe('/access-denied');
    expect(TestBed.inject(Router).url).toBe('/');
    http.expectNone('/api/v1/admin/statistics');
  });

  it('muestra los valores devueltos por la API y no los del prototipo', async () => {
    const fixture = TestBed.createComponent(AdminStatistics);
    fixture.detectChanges();
    expect(fixture.nativeElement.querySelectorAll('[data-metric]').length).toBe(0);
    http.expectOne('/api/v1/admin/statistics').flush({
      totalUsuarios: 6,
      totalMedicos: 2,
      totalCitasMes: 9,
      totalRoles: 4,
      mes: '2026-10',
    });
    await fixture.whenStable();
    const metric = (name: string) =>
      fixture.nativeElement.querySelector(`[data-metric="${name}"]`).textContent.trim();
    expect(metric('users')).toBe('6');
    expect(metric('doctors')).toBe('2');
    expect(metric('appointments')).toBe('9');
    expect(metric('roles')).toBe('4');
    expect(fixture.nativeElement.textContent).toContain('2026-10');
  });

  it('permite reintentar tras un error sin mostrar ceros como datos reales', async () => {
    const fixture = TestBed.createComponent(AdminStatistics);
    fixture.detectChanges();
    http
      .expectOne('/api/v1/admin/statistics')
      .flush({ message: 'No disponible' }, { status: 503, statusText: 'Unavailable' });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No disponible');
    expect(fixture.nativeElement.querySelectorAll('[data-metric]').length).toBe(0);
    fixture.nativeElement.querySelector('button').click();
    http.expectOne('/api/v1/admin/statistics').flush({
      totalUsuarios: 0,
      totalMedicos: 0,
      totalCitasMes: 0,
      totalRoles: 0,
      mes: '2026-10',
    });
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelectorAll('[data-metric]').length).toBe(4);
  });
});
