import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { Medico } from '../../core/models/medico';
import { MedicoService } from '../../core/services/medico';

@Component({
  selector: 'app-doctors',
  templateUrl: './doctors.page.html',
  styleUrls: ['./doctors.page.scss'],
  standalone: false,
})
export class DoctorsPage implements OnInit {

  medicos: Medico[] = [];

  loading = false;
  errorMessage = '';

  constructor(
    private medicoService: MedicoService
  ) {}

  ngOnInit(): void {
    this.cargarMedicos();
  }

  cargarMedicos(): void {

    this.loading = true;
    this.errorMessage = '';

    this.medicoService.obtenerMedicos().subscribe({
      next: (medicos) => {
        this.medicos = medicos;
        this.loading = false;
      },

      error: (error: HttpErrorResponse) => {
        this.loading = false;

        this.errorMessage =
          error.error?.message ||
          'No fue posible cargar los médicos.';
      }
    });
  }

}
