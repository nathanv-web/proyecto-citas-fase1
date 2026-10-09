import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Specialty } from '../models/specialty.model';

@Injectable({
  providedIn: 'root'
})
export class SpecialtyService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8081/api/v1/especialidades';

  getAll(): Observable<Specialty[]> {
    return this.http.get<Specialty[]>(this.apiUrl);
  }

  create(request: Specialty): Observable<Specialty> {
    return this.http.post<Specialty>(
      this.apiUrl,
      request
    );
  }
}
