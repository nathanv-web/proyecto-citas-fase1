import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';

import { Medico as MedicoModel } from '../models/medico';

@Injectable({
  providedIn: 'root'
})
export class MedicoService {

  private readonly apiUrl =
    'http://localhost:8081/api/v1/medicos';

  constructor(
    private http: HttpClient
  ) {}

  obtenerMedicos(
    especialidad?: string
  ): Observable<MedicoModel[]> {

    let params = new HttpParams();

    if (especialidad?.trim()) {
      params = params.set(
        'especialidad',
        especialidad.trim()
      );
    }

    return this.http.get<MedicoModel[]>(
      this.apiUrl,
      { params }
    );
  }
}