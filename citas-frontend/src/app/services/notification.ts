import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../core/config/api.config';

export interface NotificationItem {
  idNotificacion: number;
  titulo: string;
  mensaje: string;
  tipo: string;
  leida: boolean;
  fechaCreacion: string;
}

@Injectable({
  providedIn: 'root',
})
export class NotificationService {
  private apiUrl = `${API_BASE_URL}/notifications`;

  constructor(private http: HttpClient) {}

  getNotifications(): Observable<NotificationItem[]> {
    return this.http.get<NotificationItem[]>(this.apiUrl);
  }

  markAsRead(id: number): Observable<NotificationItem> {
    return this.http.patch<NotificationItem>(`${this.apiUrl}/${id}/read`, {});
  }
}
