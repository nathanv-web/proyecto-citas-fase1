import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DoctorAppointments } from './doctor-appointments';

describe('Agenda del médico', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem('auth_user', JSON.stringify({ permisos: ['VER_CITAS', 'REGISTRAR_DIAGNOSTICO'] }));
    TestBed.configureTestingModule({
      imports: [DoctorAppointments],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => { http.verify(); localStorage.clear(); });

  it('consulta y muestra las citas del médico autenticado', async () => {
    const fixture = TestBed.createComponent(DoctorAppointments);
    fixture.detectChanges();
    http.expectOne('/api/v1/appointments/my-agenda').flush([{
      idCita: 7, paciente: 'Maria Perez', horaInicio: '09:00:00', horaFin: '09:30:00',
      motivo: 'Consulta general', estado: 'PENDIENTE',
    }]);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Maria Perez');
    expect(fixture.nativeElement.textContent).toContain('Consulta general');
  });

  it('muestra el error de la API cuando no puede cargar la agenda', async () => {
    const fixture = TestBed.createComponent(DoctorAppointments);
    fixture.detectChanges();
    http.expectOne('/api/v1/appointments/my-agenda').flush(
      { message: 'No se pudo consultar la agenda' }, { status: 500, statusText: 'Server Error' },
    );
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No se pudo consultar la agenda');
  });
});
