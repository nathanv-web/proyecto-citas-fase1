import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { PatientNewAppointment } from './patient/patient-new-appointment/patient-new-appointment';
import { DoctorSchedules } from './doctor/doctor-schedules/doctor-schedules';
import { PatientSchedules } from './patient/patient-schedules/patient-schedules';

describe('Respuestas de los formularios de citas y horarios', () => {
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it.each([
    ['la reserva de citas', PatientNewAppointment, { idHorario: '4', motivo: 'Consulta general' }, '/api/v1/appointments'],
    ['la creación de horarios', DoctorSchedules, { idMedico: '2', fecha: '2026-12-01', horaInicio: '09:00', horaFin: '09:30' }, '/api/v1/schedules'],
  ])('muestra los errores del servidor en %s', async (_name, component, values, endpoint) => {
    const fixture = TestBed.createComponent<{ save(): void }>(component);
    fixture.detectChanges();
    for (const [name, value] of Object.entries(values)) {
      const input = fixture.nativeElement.querySelector(`[formControlName="${name}"]`) as HTMLInputElement;
      input.value = value;
      input.dispatchEvent(new Event('input'));
    }
    fixture.componentInstance.save();
    fixture.detectChanges();
    await fixture.whenStable();
    http.expectOne(endpoint).flush({ message: 'El horario ya no está disponible' }, { status: 409, statusText: 'Conflict' });
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('El horario ya no está disponible');
  });

  it('muestra los errores de consulta de horarios al recibirlos', async () => {
    const fixture = TestBed.createComponent(PatientSchedules);
    fixture.componentInstance.idMedico = 2;
    fixture.componentInstance.fecha = '2026-12-01';
    fixture.detectChanges();
    await fixture.whenStable();
    fixture.componentInstance.search();
    fixture.detectChanges();
    await fixture.whenStable();
    http.expectOne('/api/v1/schedules/available?idMedico=2&fecha=2026-12-01').flush(
      { message: 'No se pudo consultar el horario' }, { status: 500, statusText: 'Server Error' },
    );
    await fixture.whenStable();
    expect(fixture.nativeElement.textContent).toContain('No se pudo consultar el horario');
  });
});
