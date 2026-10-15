import { HttpClient } from '@angular/common/http';

import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from '../../core/config/api.config';

/** El modelo reúne los datos de una consulta que se presenta en la agenda del médico. */
export interface DoctorAppointment {
  idCita: number;
  paciente: string;
  horaInicio: string;
  horaFin: string;
  motivo: string;
  estado: string;
}

/** El servicio consulta la agenda del médico y envía los horarios y diagnósticos a la API. */
@Injectable({
  providedIn: 'root',
})
export class DoctorService {
  private http = inject(HttpClient);

  private apiUrl = API_BASE_URL;

  getAgenda() {
    return this.http.get<DoctorAppointment[]>(`${this.apiUrl}/appointments/my-agenda`);
  }

  createSchedule(idMedico: number, fecha: string, horaInicio: string, horaFin: string) {
    const body = {
      idMedico,
      fecha,
      horaInicio,
      horaFin,
    };

    return this.http.post(`${this.apiUrl}/schedules`, body);
  }

  registerDiagnosis(idCita: number, diagnosticoReceta: string, observaciones: string) {
    const body = {
      diagnosticoReceta,
      observaciones,
    };

    return this.http.put(`${this.apiUrl}/appointments/${idCita}/diagnosis`, body);
  }
}
