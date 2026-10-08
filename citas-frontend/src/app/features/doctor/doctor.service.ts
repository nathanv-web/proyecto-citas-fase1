import {
  HttpClient
} from '@angular/common/http';

import {
  inject,
  Injectable
} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {

  private http =
    inject(HttpClient);

  private apiUrl =
    'http://localhost:8081/api/v1';

  createSchedule(
    idMedico: number,
    fecha: string,
    horaInicio: string,
    horaFin: string
  ) {

    const body = {
      idMedico,
      fecha,
      horaInicio,
      horaFin
    };

    return this.http.post(
      `${this.apiUrl}/schedules`,
      body
    );
  }

  registerDiagnosis(
    idCita: number,
    diagnosticoReceta: string,
    observaciones: string
  ) {

    const body = {
      diagnosticoReceta,
      observaciones
    };

    return this.http.put(
      `${this.apiUrl}/appointments/${idCita}/diagnosis`,
      body
    );
  }
}