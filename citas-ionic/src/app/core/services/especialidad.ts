import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Especialidad as EspecialidadModel } from '../models/especialidad';

@Injectable({
  providedIn: 'root'
})
export class EspecialidadService {

  private readonly apiUrl =
    'http://localhost:8081/api/v1/especialidades';

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