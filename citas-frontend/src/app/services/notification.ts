import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  private apiUrl = 'http://localhost:8080/api/v1/notifications';

  constructor(private http: HttpClient) {}


  getNotifications(): Observable<any> {
    return this.http.get(`${this.apiUrl}`);
  }

 
  markAsRead(id: number): Observable<any> {
    return this.http.patch(`${this.apiUrl}/${id}/read`, {});
  }
}