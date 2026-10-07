import { TestBed } from '@angular/core/testing';
import { HorarioDisponible } from './horario-disponible';

describe('HorarioDisponible', () => {
  let service: HorarioDisponible;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(HorarioDisponible);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
