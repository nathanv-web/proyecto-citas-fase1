import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/config/api.config';

/** El servicio reúne las operaciones generales de consulta y gestión de citas. */
@Injectable({
  providedIn: 'root',
})
export class AppointmentService {
  private apiUrl = API_BASE_URL;

  constructor(private http: HttpClient) {}

  getMedicos(): Observable<any> {
    return this.http.get(`${this.apiUrl}/medicos`);
  }

  getEspecialidades(): Observable<any> {
    return this.http.get(`${this.apiUrl}/especialidades`);
  }

  getHorariosDisponibles(): Observable<any> {
    return this.http.get(`${this.apiUrl}/schedules/available`);
  }

  crearCita(datosCita: any): Observable<any> {
    return this.http.post(`${this.apiUrl}/appointments`, datosCita);
  }

  getHistorial(): Observable<any> {
    return this.http.get(`${this.apiUrl}/appointments/my-history`);
  }

  cancelarCita(id: number): Observable<any> {
    return this.http.put(`${this.apiUrl}/appointments/${id}/cancel`, {});
  }
}
