import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { PatientNewAppointment } from './patient/patient-new-appointment/patient-new-appointment';
import { DoctorSchedules } from './doctor/doctor-schedules/doctor-schedules';
import { PatientSchedules } from './patient/patient-schedules/patient-schedules';
import { authInterceptor } from '../core/auth/interceptors/auth.interceptor';
import { ToastContainer } from '../core/notifications/toast-container';

describe('Respuestas de los formularios de citas y horarios', () => {
  let http: HttpTestingController;
  let toasts: ComponentFixture<ToastContainer>;

  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem('auth_user', JSON.stringify({ correo: 'doctor@example.com' }));
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        provideRouter([]),
      ],
    });
    http = TestBed.inject(HttpTestingController);
    toasts = TestBed.createComponent(ToastContainer);
    toasts.detectChanges();
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('muestra el conflicto de reserva en las notificaciones comunes', async () => {
    const fixture = TestBed.createComponent(PatientNewAppointment);
    fixture.detectChanges();
    http.expectOne((req) => req.url === '/api/v1/medicos').flush([]);
    fixture.componentInstance.form.setValue({ idHorario: 4, motivo: 'Consulta general' });
    fixture.componentInstance.save();
    const request = http.expectOne('/api/v1/appointments');
    expect(request.request.body).toEqual({ idHorario: 4, motivo: 'Consulta general' });
    request.flush(
      { message: 'El horario ya no está disponible' },
      { status: 409, statusText: 'Conflict' },
    );
    await toasts.whenStable();
    expect(toasts.nativeElement.textContent).toContain('El horario ya no está disponible');
    expect(fixture.componentInstance.saving()).toBe(false);
  });

  it('muestra el conflicto de horario del médico autenticado', async () => {
    const fixture = TestBed.createComponent(DoctorSchedules);
    fixture.detectChanges();
    http
      .expectOne((req) => req.url === '/api/v1/medicos')
      .flush([
        { idMedico: 2, correo: 'doctor@example.com', nombre: 'Doctor', especialidad: 'General' },
      ]);
    fixture.componentInstance.form.setValue({
      idMedico: 2,
      fecha: '2026-12-01',
      horaInicio: '09:00',
      horaFin: '09:30',
    });
    fixture.componentInstance.save();
    http
      .expectOne('/api/v1/schedules')
      .flush(
        { message: 'El horario ya no está disponible' },
        { status: 409, statusText: 'Conflict' },
      );
    await toasts.whenStable();
    expect(toasts.nativeElement.textContent).toContain('El horario ya no está disponible');
    expect(fixture.componentInstance.saving()).toBe(false);
  });

  it('muestra los errores al consultar disponibilidad', async () => {
    const fixture = TestBed.createComponent(PatientSchedules);
    fixture.detectChanges();
    http.expectOne((req) => req.url === '/api/v1/medicos').flush([]);
    fixture.componentInstance.idMedico = 2;
    fixture.componentInstance.fecha = '2026-12-01';
    fixture.componentInstance.search();
    http
      .expectOne('/api/v1/schedules/available?idMedico=2&fecha=2026-12-01')
      .flush(
        { message: 'No se pudo consultar el horario' },
        { status: 500, statusText: 'Server Error' },
      );
    await toasts.whenStable();
    expect(toasts.nativeElement.textContent).toContain('No se pudo consultar el horario');
    expect(fixture.componentInstance.loading()).toBe(false);
  });
});
