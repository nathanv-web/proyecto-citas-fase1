import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../../core/config/api.config';

import { Doctor, DoctorRequest, DoctorUpdateRequest } from '../models/doctor.model';

/** El servicio administra los perfiles médicos mediante los recursos de la API. */
@Injectable({
  providedIn: 'root',
})
export class DoctorAdminService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${API_BASE_URL}/medicos`;

  getAll(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(this.apiUrl);
  }

  activate(id: number): Observable<Doctor> {
    return this.http.put<Doctor>(`${this.apiUrl}/${id}`, { estado: 'ACTIVO' });
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  create(request: DoctorRequest): Observable<Doctor> {
    return this.http.post<Doctor>(this.apiUrl, request);
  }

  update(id: number, request: DoctorUpdateRequest): Observable<Doctor> {
    return this.http.put<Doctor>(`${this.apiUrl}/${id}`, request);
  }
}
