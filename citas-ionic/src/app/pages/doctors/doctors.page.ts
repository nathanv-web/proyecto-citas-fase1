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
  medicosFiltrados: Medico[] = [];

  filtroBusqueda: string = '';
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
        this.medicosFiltrados = medicos;
        this.loading = false;
      },

      error: (error: HttpErrorResponse) => {
        this.loading = false;

        if (error.status === 0) {
          this.errorMessage = 'No se pudo conectar con el servidor.';
          return;
        }

        this.errorMessage =
          error.error?.message ||
          'No fue posible cargar los médicos.';
      }
    });
  }

  filtrarMedicos(): void {
    const termino = this.filtroBusqueda.toLowerCase().trim();

    if (!termino) {
      this.medicosFiltrados = this.medicos;
      return;
    }

    this.medicosFiltrados = this.medicos.filter((m) => {
      const nombreMatches = m.nombre ? m.nombre.toLowerCase().includes(termino) : false;
      const especialidadMatches = m.especialidad ? m.especialidad.toLowerCase().includes(termino) : false;
      return nombreMatches || especialidadMatches;
    });
  }

}