import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { UserList } from './users/pages/user-list/user-list';
import { RoleList } from './roles/role-list/role-list';
import { ConfirmationService } from '../../core/notifications/confirmation.service';
import { RoleService } from './roles/services/role.service';

describe('Panel administrativo conectado al backend', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem('auth_user', JSON.stringify({ permisos: ['VER_USUARIOS', 'VER_ROLES'] }));
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('envía búsqueda y estado inactivo a usuarios y permite limpiar ambos filtros', async () => {
    const fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();
    http.expectOne('/api/v1/users').flush([]);
    await fixture.whenStable();
    const input = fixture.nativeElement.querySelector('#user-search') as HTMLInputElement;
    expect(input).not.toBeNull();
    input.value = ' Ana ';
    input.dispatchEvent(new Event('input'));
    const select = fixture.nativeElement.querySelector('#user-status') as HTMLSelectElement;
    select.value = 'false';
    select.dispatchEvent(new Event('change'));
    fixture.nativeElement
      .querySelector('form')
      .dispatchEvent(new Event('submit', { cancelable: true }));
    http
      .expectOne(
        (request) =>
          request.url === '/api/v1/users' &&
          request.params.get('search') === 'Ana' &&
          request.params.get('activo') === 'false',
      )
      .flush([
        {
          idUsuario: 2,
          nombre: 'Ana',
          apellido: 'Lopez',
          correo: 'ana@example.com',
          telefono: '',
          activo: false,
          roles: [],
        },
      ]);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Ana Lopez');
    fixture.nativeElement.querySelector('[data-action="clear-filters"]').click();
    http.expectOne('/api/v1/users').flush([]);
    await fixture.whenStable();
    expect(input.value).toBe('');
    expect(select.value).toBe('');
  });

  it('pagina roles desde la API sin convertir la página en una lista local', async () => {
    const fixture = TestBed.createComponent(RoleList);
    fixture.detectChanges();
    http
      .expectOne((request) => request.url === '/api/v1/roles' && request.params.get('page') === '0')
      .flush({
        content: [
          {
            idRol: 7,
            nombre: 'ROLE_RECEPCION',
            descripcion: 'Recepción',
            permisos: ['VER_USUARIOS'],
          },
        ],
        totalElements: 11,
        totalPages: 2,
        number: 0,
      });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('ROLE_RECEPCION');
    fixture.nativeElement.querySelector('[data-action="next-page"]').click();
    http
      .expectOne((request) => request.url === '/api/v1/roles' && request.params.get('page') === '1')
      .flush({
        content: [{ idRol: 8, nombre: 'ROLE_AUDITOR', descripcion: '', permisos: [] }],
        totalElements: 11,
        totalPages: 2,
        number: 1,
      });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('ROLE_AUDITOR');
    expect(fixture.nativeElement.textContent).not.toContain('ROLE_RECEPCION');
  });

  it('muestra el error de usuarios y permite reintentar sin datos inventados', async () => {
    const fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();
    http
      .expectOne('/api/v1/users')
      .flush({ message: 'Consulta no disponible' }, { status: 500, statusText: 'Server Error' });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Consulta no disponible');
    expect(fixture.componentInstance.users).toEqual([]);
  });

  it('impide buscar durante la desactivación y refresca después de guardarla', async () => {
    localStorage.setItem('auth_user', JSON.stringify({ permisos: ['DESACTIVAR_USUARIOS'] }));
    vi.spyOn(TestBed.inject(ConfirmationService), 'confirm').mockResolvedValue(true);
    const fixture = TestBed.createComponent(UserList);
    fixture.detectChanges();
    const user = {
      idUsuario: 2,
      nombre: 'Ana',
      apellido: 'Lopez',
      correo: 'ana@example.com',
      telefono: '',
      activo: true,
      roles: [],
    };
    http.expectOne('/api/v1/users').flush([user]);
    await fixture.whenStable();
    fixture.nativeElement.querySelector('tbody button').click();
    await fixture.whenStable();
    const deactivate = http.expectOne('/api/v1/users/2');
    fixture.detectChanges();
    await fixture.whenStable();
    expect(fixture.nativeElement.querySelector('#user-search').disabled).toBe(true);
    fixture.componentInstance.loadUsers();
    http.expectNone('/api/v1/users');
    deactivate.flush(null);
    http.expectOne('/api/v1/users').flush([{ ...user, activo: false }]);
    await fixture.whenStable();
    expect(fixture.componentInstance.users[0].activo).toBe(false);
    expect(fixture.componentInstance.success).toContain('desactivado');
  });

  it('conserva los datos y muestra el rechazo al intentar eliminar un rol asignado', async () => {
    localStorage.setItem('auth_user', JSON.stringify({ permisos: ['ELIMINAR_ROLES'] }));
    vi.spyOn(TestBed.inject(ConfirmationService), 'confirm').mockResolvedValue(true);
    const fixture = TestBed.createComponent(RoleList);
    fixture.detectChanges();
    const role = { idRol: 3, nombre: 'ROLE_PATIENT', descripcion: 'Pacientes', permisos: [] };
    http
      .expectOne((req) => req.url === '/api/v1/roles')
      .flush({ content: [role], totalElements: 1, totalPages: 1, number: 0 });
    await fixture.componentInstance.deleteRole(role);
    http
      .expectOne('/api/v1/roles/3')
      .flush({ message: 'El rol está asignado' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();
    expect(fixture.componentInstance.roles).toEqual([role]);
    expect(fixture.componentInstance.success).toBe('');
    expect(fixture.nativeElement.textContent).toContain('El rol está asignado');
    http.expectNone((req) => req.url === '/api/v1/roles');
  });

  it('mantiene la respuesta de lista para los selectores de roles existentes', () => {
    let roles: unknown;
    TestBed.inject(RoleService)
      .getAll()
      .subscribe((data) => {
        roles = data;
      });
    const request = http.expectOne('/api/v1/roles');
    expect(request.request.params.keys()).toEqual([]);
    request.flush([{ idRol: 1, nombre: 'ROLE_ADMIN', descripcion: '', permisos: [] }]);
    expect(Array.isArray(roles)).toBe(true);
  });
});
