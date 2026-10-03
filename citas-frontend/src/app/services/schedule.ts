import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Schedule } from '../models/schedule.model';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl = 'http://localhost:8081/api/v1';

  createSchedule(schedule: Schedule): Observable<unknown> {
    return this.http.post(
      `${this.apiUrl}/schedules`,
      schedule
    );
  }
}
