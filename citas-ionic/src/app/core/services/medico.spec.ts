import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { MedicoService } from './medico';

describe('MedicoService', () => {
  let service: MedicoService;

  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [provideHttpClient()] });
    service = TestBed.inject(MedicoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
