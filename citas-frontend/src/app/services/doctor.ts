import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Appointment } from '../models/appointment.model';
import { Doctor } from '../models/doctor.model';
import { DiagnosisRequest } from '../models/diagnosis.model';

@Injectable({
  providedIn: 'root'
})
export class DoctorService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8081/api/v1';

  getDoctors(): Observable<Doctor[]> {
    return this.http.get<Doctor[]>(
      `${this.apiUrl}/medicos`
    );
  }

  getAppointments(): Observable<Appointment[]> {
    return this.http.get<Appointment[]>(
      `${this.apiUrl}/appointments/doctor`
    );
  }

  registerDiagnosis(
    appointmentId: number,
    diagnosis: DiagnosisRequest
  ): Observable<Appointment> {
    return this.http.put<Appointment>(
      `${this.apiUrl}/appointments/${appointmentId}/diagnosis`,
      diagnosis
    );
  }
}