import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { Login } from './login';
import { AccessDenied } from '../access-denied/access-denied';

describe('Inicio de sesión', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(), provideHttpClientTesting(),
        provideRouter([
          { path: 'login', component: Login },
          { path: 'panels', component: AccessDenied },
          { path: 'admin', component: AccessDenied },
          { path: 'admin/users', component: AccessDenied },
          { path: 'doctor', component: AccessDenied },
          { path: 'patient', component: AccessDenied },
          { path: 'access-denied', component: AccessDenied },
        ]),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it.each([
    ['ROLE_ADMIN', ['VER_USUARIOS']],
    ['ROLE_DOCTOR', ['VER_CITAS']],
    ['ROLE_PATIENT', ['VER_HISTORIAL']],
    ['ROLE_JAIME', ['VER_USUARIOS']],
    ['ROLE_SIN_PERMISOS', []],
  ])('lleva al usuario %s al selector sin decidir por su rol', async (role, permisos) => {
    const fixture = TestBed.createComponent(Login);
    fixture.componentInstance.loginForm.setValue({ email: 'ana@example.com', password: '12345678' });
    fixture.componentInstance.iniciarSesion();
    const request = http.expectOne(req => req.url.endsWith('/auth/login'));
    expect(request.request.body).toEqual({ email: 'ana@example.com', password: '12345678' });
    request.flush({
      token: 'new-token', tipo: 'Bearer',
      usuario: { idUsuario: 1, nombre: 'Ana', apellido: 'Lopez', correo: 'ana@example.com', roles: [role], permisos },
    });
    await fixture.whenStable();
    expect(TestBed.inject(Router).url).toBe('/panels');
    expect(localStorage.getItem('auth_token')).toBe('new-token');
  });

  it('muestra el error de credenciales y permite volver a intentarlo', async () => {
    const fixture = TestBed.createComponent(Login);
    fixture.componentInstance.loginForm.setValue({ email: 'ana@example.com', password: 'incorrecta' });
    fixture.componentInstance.iniciarSesion();
    http.expectOne(req => req.url.endsWith('/auth/login')).flush({}, { status: 401, statusText: 'Unauthorized' });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Correo o contraseña incorrectos.');
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
