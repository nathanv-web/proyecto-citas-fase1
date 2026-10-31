import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../../core/config/api.config';

import { Specialty } from '../models/specialty.model';

/** El servicio consulta, crea y cambia el estado de las especialidades médicas. */
@Injectable({
  providedIn: 'root',
})
export class SpecialtyService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${API_BASE_URL}/especialidades`;

  getAll(): Observable<Specialty[]> {
    return this.http.get<Specialty[]>(this.apiUrl);
  }

  activate(id: number): Observable<Specialty> {
    return this.http.patch<Specialty>(`${this.apiUrl}/${id}/estado`, { activo: true });
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  create(request: Specialty): Observable<Specialty> {
    return this.http.post<Specialty>(this.apiUrl, request);
  }

  update(id: number, request: Specialty): Observable<Specialty> {
    return this.http.put<Specialty>(`${this.apiUrl}/${id}`, request);
  }
}
