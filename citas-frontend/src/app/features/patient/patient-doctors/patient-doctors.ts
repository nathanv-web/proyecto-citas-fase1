import { Component, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, catchError, debounceTime, of, switchMap, tap } from 'rxjs';
import { AppointmentService, Medico } from '../appointment.service';

@Component({
  selector: 'app-patient-doctors',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './patient-doctors.html',
})
export class PatientDoctors implements OnInit {
  private readonly service = inject(AppointmentService);
  private readonly destroyRef = inject(DestroyRef);
  private readonly filters = new Subject<void>();
  readonly doctors = signal<Medico[]>([]);
  readonly loading = signal(false);
  search = '';
  especialidad = '';
  estado = '';

  ngOnInit(): void {
    this.filters
      .pipe(
        debounceTime(300),
        tap(() => this.loading.set(true)),
        switchMap(() =>
          this.service
            .getDoctors(this.search, this.especialidad, this.estado)
            .pipe(catchError(() => of([] as Medico[]))),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((data) => {
        this.doctors.set(data);
        this.loading.set(false);
      });
    this.filter();
  }
  filter(): void {
    this.filters.next();
  }
}
