import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AppointmentService } from './appointment';

describe('AppointmentService (compatibilidad)', () => {
  let service: AppointmentService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AppointmentService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('consulta los médicos en la misma API que los demás módulos', () => {
    service.getMedicos().subscribe();
    const request = http.expectOne(req => req.url.endsWith('/medicos'));
    expect(request.request.url).toBe('/api/v1/medicos');
    request.flush([]);
  });
});
