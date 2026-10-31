import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../core/config/api.config';

import { AppointmentCreateRequest } from '../models/appointment-create-request';
import { AppointmentResponse } from '../models/appointment-response';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {

  private readonly apiUrl =
  `${API_BASE_URL}/appointments`;

  constructor(
    private http: HttpClient
  ) {}

  crearCita(
    request: AppointmentCreateRequest
  ): Observable<AppointmentResponse> {

    return this.http.post<AppointmentResponse>(
      this.apiUrl,
      request
    );
  }

  obtenerHistorial(): Observable<AppointmentResponse[]> {

    return this.http.get<AppointmentResponse[]>(
      `${this.apiUrl}/my-history`
    );
  }

  reprogramarCita(
    idCita: number,
    idHorarioNuevo: number
  ): Observable<AppointmentResponse> {

    return this.http.put<AppointmentResponse>(
      `${this.apiUrl}/${idCita}/reschedule`,
      { idHorarioNuevo }
    );
  }

  cancelarCita(
    idCita: number,
    motivoCancelacion: string
  ): Observable<AppointmentResponse> {

    return this.http.put<AppointmentResponse>(
      `${this.apiUrl}/${idCita}/cancel`,
      { motivoCancelacion }
    );
  }
}
