import { Component, inject, OnInit, signal } from '@angular/core';
import { finalize } from 'rxjs';
import { apiErrorMessage } from '../../../core/notifications/api-error';
import { Statistics } from './models/statistics.model';
import { StatisticsService } from './services/statistics.service';

@Component({
  selector: 'app-admin-statistics',
  standalone: true,
  templateUrl: './admin-statistics.html',
  styleUrl: './admin-statistics.css',
})
export class AdminStatistics implements OnInit {
  private readonly service = inject(StatisticsService);
  readonly summary = signal<Statistics | null>(null);
  readonly loading = signal(false);
  readonly error = signal('');

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    if (this.loading()) return;
    this.loading.set(true);
    this.error.set('');
    this.summary.set(null);
    this.service
      .getSummary()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (summary) => this.summary.set(summary),
        error: (error) => this.error.set(apiErrorMessage(error)),
      });
  }
}
