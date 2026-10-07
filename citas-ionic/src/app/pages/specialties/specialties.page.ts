import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';

import { Especialidad } from '../../core/models/especialidad';
import { EspecialidadService } from '../../core/services/especialidad';

@Component({
  selector: 'app-specialties',
  templateUrl: './specialties.page.html',
  styleUrls: ['./specialties.page.scss'],
  standalone: false,
})
export class SpecialtiesPage implements OnInit {

  especialidades: Especialidad[] = [];

  loading = false;
  errorMessage = '';

  constructor(
    private especialidadService: EspecialidadService
  ) {}

  ngOnInit(): void {
    this.cargarEspecialidades();
  }

  cargarEspecialidades(): void {

    this.loading = true;
    this.errorMessage = '';

    this.especialidadService
      .obtenerEspecialidades()
      .subscribe({
        next: (especialidades) => {
          this.especialidades = especialidades;
          this.loading = false;
        },

        error: (error: HttpErrorResponse) => {
          this.loading = false;

          this.errorMessage =
            error.error?.message ||
            'No fue posible cargar las especialidades.';
        }
      });
  }

}