import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HorarioDisponibleService } from './horario-disponible';

describe('HorarioDisponibleService', () => {
  let service: HorarioDisponibleService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(HorarioDisponibleService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
