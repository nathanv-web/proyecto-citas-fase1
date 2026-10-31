import { HttpClient, HttpParams } from '@angular/common/http';

import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from '../../core/config/api.config';
import { Horario } from '../patient/appointment.service';

/** El modelo reúne los datos de una consulta que se presenta en la agenda del médico. */
export interface DoctorAppointment {
  fecha: string;
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

  getAgenda(fecha?: string, periodo = 'dia') {
    let params = new HttpParams().set('periodo', periodo);
    if (fecha) params = params.set('fecha', fecha);
    return this.http.get<DoctorAppointment[]>(`${this.apiUrl}/appointments/my-agenda`, { params });
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

  getSchedules() {
    return this.http.get<Horario[]>(`${this.apiUrl}/schedules/my-schedules`);
  }

  registerDiagnosis(idCita: number, diagnosticoReceta: string, observaciones: string) {
    const body = {
      diagnosticoReceta,
      observaciones,
    };

    return this.http.put(`${this.apiUrl}/appointments/${idCita}/diagnosis`, body);
  }
}
