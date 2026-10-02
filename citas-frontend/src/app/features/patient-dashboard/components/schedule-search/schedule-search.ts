import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppointmentService } from '../../../../services/appointment';

@Component({
  selector: 'app-schedule-search',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './schedule-search.html',
  styleUrl: './schedule-search.css'
})
export class ScheduleSearchComponent implements OnInit {
  horarios: any[] = [];

  constructor(private appointmentService: AppointmentService) {}

  ngOnInit(): void {
   
    this.buscarHorariosDisponibles();
  }

  buscarHorariosDisponibles() {
    this.appointmentService.getHorariosDisponibles().subscribe({
      next: (datos) => this.horarios = datos,
      error: (err) => console.error('Error al cargar horarios disponibles', err)
    });
  }
}