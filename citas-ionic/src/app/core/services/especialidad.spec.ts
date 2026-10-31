import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { EspecialidadService } from './especialidad';

describe('EspecialidadService', () => {
  let service: EspecialidadService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(EspecialidadService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
