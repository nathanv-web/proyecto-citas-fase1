import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { DoctorAppointments } from './doctor-appointments';
import { provideRouter } from '@angular/router';
import { authInterceptor } from '../../../core/auth/interceptors/auth.interceptor';
import { ToastContainer } from '../../../core/notifications/toast-container';

describe('Agenda del médico', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem(
      'auth_user',
      JSON.stringify({ permisos: ['VER_CITAS', 'REGISTRAR_DIAGNOSTICO'] }),
    );
    TestBed.configureTestingModule({
      imports: [DoctorAppointments],
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('consulta y muestra las citas del médico autenticado', async () => {
    const fixture = TestBed.createComponent(DoctorAppointments);
    fixture.detectChanges();
    const request = http.expectOne((req) => req.url === '/api/v1/appointments/my-agenda');
    expect(request.request.params.get('periodo')).toBe('dia');
    expect(request.request.params.get('fecha')).toBe(fixture.componentInstance.fecha);
    request.flush([
      {
        idCita: 7,
        paciente: 'Maria Perez',
        horaInicio: '09:00:00',
        horaFin: '09:30:00',
        motivo: 'Consulta general',
        estado: 'PENDIENTE',
      },
    ]);
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('Maria Perez');
    expect(fixture.nativeElement.textContent).toContain('Consulta general');
  });

  it('muestra el error de la API cuando no puede cargar la agenda', async () => {
    const toasts = TestBed.createComponent(ToastContainer);
    toasts.detectChanges();
    const fixture = TestBed.createComponent(DoctorAppointments);
    fixture.detectChanges();
    http
      .expectOne((req) => req.url === '/api/v1/appointments/my-agenda')
      .flush(
        { message: 'No se pudo consultar la agenda' },
        { status: 500, statusText: 'Server Error' },
      );
    await toasts.whenStable();
    expect(toasts.nativeElement.textContent).toContain('No se pudo consultar la agenda');
  });
});
