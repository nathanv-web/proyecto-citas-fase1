import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

import { Especialidad as EspecialidadModel } from '../models/especialidad';

@Injectable({
  providedIn: 'root'
})
export class EspecialidadService {

  private readonly apiUrl =
    `${API_BASE_URL}/especialidades`;

  constructor(
    private http: HttpClient
  ) {}

  obtenerEspecialidades():
    Observable<EspecialidadModel[]> {

    return this.http.get<EspecialidadModel[]>(
      this.apiUrl
    );
  }
}
