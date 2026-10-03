import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { DoctorService } from '../../../services/doctor';
import { DiagnosisRequest } from '../../../models/diagnosis.model';

@Component({
  selector: 'app-diagnosis',
  standalone: true,
  imports: [FormsModule, RouterLink],
  templateUrl: './diagnosis.html',
  styleUrl: './diagnosis.css'
})
export class Diagnosis {

  private readonly doctorService = inject(DoctorService);
  private readonly route = inject(ActivatedRoute);

  appointmentId = 0;

  diagnosis: DiagnosisRequest = {
    diagnosticoReceta: '',
    observaciones: ''
  };

  mensaje = '';
  error = '';
  guardando = false;

  constructor() {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.appointmentId = Number(id);
    }
  }

  registrarDiagnostico(): void {
    this.mensaje = '';
    this.error = '';

    if (!this.appointmentId) {
      this.error = 'No se encontró el ID de la cita.';
      return;
    }

    if (!this.diagnosis.diagnosticoReceta.trim()) {
      this.error = 'El diagnóstico y la receta son obligatorios.';
      return;
    }

    this.guardando = true;

    this.doctorService
      .registerDiagnosis(this.appointmentId, this.diagnosis)
      .subscribe({
        next: () => {
          this.mensaje = 'Diagnóstico registrado correctamente.';
          this.diagnosis = {
            diagnosticoReceta: '',
            observaciones: ''
          };
          this.guardando = false;
        },
        error: (error) => {
          console.error(error);

          this.error =
            error?.error?.message ||
            'No se pudo registrar el diagnóstico.';

          this.guardando = false;
        }
      });
  }
}
