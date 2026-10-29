import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../../core/config/api.config';
import { Statistics } from '../models/statistics.model';

@Injectable({ providedIn: 'root' })
export class StatisticsService {
  private readonly http = inject(HttpClient);

  getSummary(): Observable<Statistics> {
    return this.http.get<Statistics>(`${API_BASE_URL}/admin/statistics`);
  }
}
