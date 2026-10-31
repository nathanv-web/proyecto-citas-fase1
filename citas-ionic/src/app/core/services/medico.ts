import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

import { Medico as MedicoModel } from '../models/medico';

@Injectable({
  providedIn: 'root'
})
export class MedicoService {

  private readonly apiUrl =
    `${API_BASE_URL}/medicos`;

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
