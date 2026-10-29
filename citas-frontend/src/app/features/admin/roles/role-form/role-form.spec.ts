import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../../../app.routes';
import { RoleForm } from './role-form';
import { AuthService } from '../../../../core/auth/services/auth.service';
import { of } from 'rxjs';

describe('Edición de roles con permisos limitados', () => {
  let http: HttpTestingController;
  beforeEach(() => {
    localStorage.setItem('auth_token', 'test-token');
    localStorage.setItem(
      'auth_user',
      JSON.stringify({
        idUsuario: 1,
        nombre: 'Ana',
        apellido: 'Lopez',
        correo: 'ana@example.com',
        roles: ['ROLE_ASSISTANT'],
        permisos: ['ACCESO_ADMIN', 'CREAR_ROLES', 'ACTUALIZAR_ROLES', 'VER_ROLES'],
      }),
    );
    TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    const auth = TestBed.inject(AuthService);
    vi.spyOn(auth, 'refreshSession').mockReturnValue(
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

  it('crea el rol sin consultar ni asignar permisos ajenos a su autorización', async () => {
    const harness = await RouterTestingHarness.create();
    const form = await harness.navigateByUrl('/admin/roles/new', RoleForm);
    http.expectNone('/api/v1/permissions');
    form.form = { nombre: 'ROLE_RECEPTION', descripcion: 'Recepción' };
    form.save();
    http
      .expectOne('/api/v1/roles')
      .flush({ idRol: 20, nombre: 'ROLE_RECEPTION', descripcion: 'Recepción', permisos: [] });
    http.expectNone('/api/v1/roles/20/permissions');
    await harness.fixture.whenStable();
    expect(form.saving).toBe(false);
    expect(form.success).toContain('Rol creado');
  });

  it('actualiza los datos del rol sin reemplazar sus permisos', async () => {
    const harness = await RouterTestingHarness.create();
    const form = await harness.navigateByUrl('/admin/roles/edit/20', RoleForm);
    http.expectNone('/api/v1/permissions');
    http
      .expectOne('/api/v1/roles/20')
      .flush({
        idRol: 20,
        nombre: 'ROLE_RECEPTION',
        descripcion: 'Recepción',
        permisos: ['VER_MEDICOS'],
      });
    form.form.descripcion = 'Recepción actualizada';
    form.save();
    const update = http.expectOne('/api/v1/roles/20');
    expect(update.request.body).toEqual({
      nombre: 'ROLE_RECEPTION',
      descripcion: 'Recepción actualizada',
    });
    update.flush({
      idRol: 20,
      nombre: 'ROLE_RECEPTION',
      descripcion: 'Recepción actualizada',
      permisos: ['VER_MEDICOS'],
    });
    http.expectNone('/api/v1/roles/20/permissions');
    await harness.fixture.whenStable();
    expect(form.saving).toBe(false);
    expect(form.success).toContain('actualizado');
  });
});
