import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Doctor,
  DoctorRequest
} from '../models/doctor.model';

@Injectable({
  providedIn: 'root'
})
export class DoctorAdminService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8081/api/v1/medicos';

  getAll(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(
      this.apiUrl
    );
  }

  create(
    request: DoctorRequest
  ): Observable<Doctor> {

    return this.http.post<Doctor>(
      this.apiUrl,
      request
    );
  }
}
