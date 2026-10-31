import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { ActivatedRoute, provideRouter } from '@angular/router';
import { DoctorList } from './doctor-list';

describe('DoctorList', () => {
  afterEach(() => {
    localStorage.clear();
  });

  it('uses reactive validation for doctor creation', () => {
    localStorage.setItem(
      'auth_user',
      JSON.stringify({ permisos: ['CREAR_MEDICOS', 'VER_USUARIOS', 'VER_ESPECIALIDADES'] }),
    );
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { data: { createDoctor: true } } } },
      ],
    });

    const fixture = TestBed.createComponent(DoctorList);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/especialidades').flush([]);
    http.expectOne('/api/v1/users').flush([]);

    const form = fixture.componentInstance.doctorForm;
    expect(form.invalid).toBe(true);
    form.patchValue({
      idUsuario: 1,
      idEspecialidad: 2,
      colegiado: 'COL-1234',
      aniosExperiencia: 0,
      biografia: '',
    });
    expect(form.valid).toBe(true);
    form.patchValue({ colegiado: '1234' });
    expect(form.invalid).toBe(true);
    form.patchValue({ colegiado: 'COL-1234' });
    fixture.componentInstance.createDoctor();
    const request = http.expectOne('/api/v1/medicos');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      idUsuario: 1,
      idEspecialidad: 2,
      colegiado: 'COL-1234',
      aniosExperiencia: 0,
      biografia: '',
    });
    request.flush({ idMedico: 3 });
    expect(fixture.componentInstance.success).toContain('creado');
    http.verify();
  });

  it('saves doctor profile edits through the existing update endpoint', () => {
    localStorage.setItem(
      'auth_user',
      JSON.stringify({ permisos: ['VER_MEDICOS', 'ACTUALIZAR_MEDICOS'] }),
    );
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([]),
        { provide: ActivatedRoute, useValue: { snapshot: { data: {} } } },
      ],
    });

    const fixture = TestBed.createComponent(DoctorList);
    fixture.detectChanges();
    const http = TestBed.inject(HttpTestingController);
    http.expectOne('/api/v1/medicos').flush([
      {
        idMedico: 4,
        nombre: 'Ana Médico',
        correo: 'ana@example.com',
        especialidad: 'Medicina General',
        colegiado: 'COL-1234',
        aniosExperiencia: 4,
        biografia: 'Experiencia previa',
        estado: 'ACTIVO',
      },
    ]);
    http.expectOne('/api/v1/especialidades').flush([
      { idEspecialidad: 2, nombre: 'Medicina General', descripcion: '', activo: true },
    ]);

    fixture.componentInstance.startEditDoctor(fixture.componentInstance.doctors[0]);
    fixture.componentInstance.doctorEditForm.patchValue({
      idEspecialidad: 2,
      colegiado: 'COL-9999',
      aniosExperiencia: 5,
      biografia: 'Perfil actualizado',
    });
    fixture.componentInstance.updateDoctor();

    const request = http.expectOne('/api/v1/medicos/4');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      idEspecialidad: 2,
      colegiado: 'COL-9999',
      aniosExperiencia: 5,
      biografia: 'Perfil actualizado',
    });
    request.flush({ idMedico: 4 });
    expect(fixture.componentInstance.editingDoctorId).toBeNull();
    http.expectOne('/api/v1/medicos').flush([]);
    http.verify();
  });
});
