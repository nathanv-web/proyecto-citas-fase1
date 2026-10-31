import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { SpecialtyList } from './specialty-list';

describe('SpecialtyList', () => {
  afterEach(() => localStorage.clear());

  it('updates an existing specialty through the backend endpoint', () => {
    localStorage.setItem(
      'auth_user',
      JSON.stringify({ permisos: ['VER_ESPECIALIDADES', 'ACTUALIZAR_ESPECIALIDADES'] }),
    );
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { data: {} } } },
      ],
    });
    const fixture = TestBed.createComponent(SpecialtyList);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/especialidades').flush([
      { idEspecialidad: 2, nombre: 'Medicina General', descripcion: 'Atención primaria', activo: true },
    ]);

    fixture.componentInstance.startEditSpecialty(fixture.componentInstance.specialties[0]);
    fixture.componentInstance.specialtyEditForm.patchValue({
      nombre: 'Medicina familiar',
      descripcion: 'Atención integral',
    });
    fixture.componentInstance.updateSpecialty();

    const request = http.expectOne('/api/v1/especialidades/2');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ nombre: 'Medicina familiar', descripcion: 'Atención integral' });
    request.flush({ idEspecialidad: 2, nombre: 'Medicina familiar', descripcion: 'Atención integral' });
    expect(fixture.componentInstance.editingSpecialtyId).toBeNull();
    http.expectOne('/api/v1/especialidades').flush([]);
    http.verify();
  });
});
