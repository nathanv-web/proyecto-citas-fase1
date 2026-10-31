import { Injectable } from '@angular/core';
import {
  HttpClient,
  HttpParams
} from '@angular/common/http';

import { Observable } from 'rxjs';
import { API_BASE_URL } from '../config/api.config';

import { HorarioDisponible } from '../models/horario-disponible';

@Injectable({
  providedIn: 'root'
})
export class HorarioDisponibleService {

  private readonly apiUrl =
    `${API_BASE_URL}/schedules/available`;

  constructor(
    private http: HttpClient
  ) {}

  obtenerHorariosDisponibles(
    idMedico: number,
    fecha: string
  ): Observable<HorarioDisponible[]> {

    const params = new HttpParams()
      .set('idMedico', idMedico.toString())
      .set('fecha', fecha);

    return this.http.get<HorarioDisponible[]>(
      this.apiUrl,
      { params }
    );
  }
}
