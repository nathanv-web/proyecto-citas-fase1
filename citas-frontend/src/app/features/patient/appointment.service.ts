import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from '../../core/config/api.config';

/** El modelo reúne los datos profesionales del médico que aparecen en el directorio. */
export interface Medico {
  idMedico: number;
  nombre: string;
  correo: string;
  especialidad: string;
  colegiado: string;
  aniosExperiencia: number;
  biografia: string;
  estado: string;
}

/** El modelo describe un horario de atención, el médico asociado y su disponibilidad. */
export interface Horario {
  idHorario: number;
  idMedico: number;
  medico: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: string;
}

/** El modelo describe una cita y la información clínica disponible en su historial. */
export interface Cita {
  idCita: number;
  paciente: string;
  medico: string;
  especialidad: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: string;
  motivo: string;
  diagnosticoReceta: string;
  observaciones: string;
}

/** El servicio reúne las consultas del paciente y las operaciones de reserva y cancelación de citas. */
@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  private http = inject(HttpClient);

  private apiUrl = API_BASE_URL;

  getDoctors() {
    return this.http.get<Medico[]>(`${this.apiUrl}/medicos`);
  }

  getSpecialties() {
    return this.http.get<any[]>(`${this.apiUrl}/especialidades`);
  }

  getAvailableSchedules(idMedico: number, fecha: string) {
    const params = new HttpParams().set('idMedico', idMedico).set('fecha', fecha);

    return this.http.get<Horario[]>(`${this.apiUrl}/schedules/available`, { params });
  }

  createAppointment(idHorario: number, motivo: string) {
    const body = {
      idHorario,
      motivo,
    };

    return this.http.post<Cita>(`${this.apiUrl}/appointments`, body);
  }

  getHistory() {
    return this.http.get<Cita[]>(`${this.apiUrl}/appointments/my-history`);
  }

  cancelAppointment(idCita: number) {
    return this.http.put<Cita>(`${this.apiUrl}/appointments/${idCita}/cancel`, {});
  }
}
