import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AppointmentService } from '../../services/appointment';
import { AppointmentHistoryComponent } from './components/appointment-history/appointment-history';
import { ScheduleSearchComponent } from './components/schedule-search/schedule-search';
import { AppointmentFormComponent } from './components/appointment-form/appointment-form';
@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [CommonModule, AppointmentHistoryComponent, ScheduleSearchComponent, AppointmentFormComponent],
  templateUrl: './patient-dashboard.html',
  styleUrl: './patient-dashboard.css'
})
export class PatientDashboardComponent implements OnInit {
  medicos: any[] = [];
  especialidades: any[] = [];

 
  constructor(private appointmentService: AppointmentService) {}

  
  ngOnInit(): void {
    this.cargarDatosIniciales();
  }

  cargarDatosIniciales() {
    
    this.appointmentService.getMedicos().subscribe({
      next: (datos) => this.medicos = datos,
      error: (err) => console.error('Error al cargar médicos', err)
    });

   
    this.appointmentService.getEspecialidades().subscribe({
      next: (datos) => this.especialidades = datos,
      error: (err) => console.error('Error al cargar especialidades', err)
    });
  }
}