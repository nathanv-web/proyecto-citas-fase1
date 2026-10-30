import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
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

  filtroBusqueda = '';

  loading = false;
  errorMessage = '';

  constructor(
    private medicoService: MedicoService,
    private cdr: ChangeDetectorRef
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

        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {

        this.loading = false;

        if (error.status === 0) {
          this.errorMessage =
            'No se pudo conectar con el servidor.';
        } else {
          this.errorMessage =
            error.error?.message ??
            'No fue posible cargar los médicos.';
        }

        this.cdr.markForCheck();
      }

    });
  }

  filtrarMedicos(): void {

    const termino =
      this.filtroBusqueda
        .toLowerCase()
        .trim();

    if (!termino) {
      this.medicosFiltrados = this.medicos;
      this.cdr.markForCheck();
      return;
    }

    this.medicosFiltrados =
      this.medicos.filter((medico) => {

        const coincideNombre =
          medico.nombre
            ?.toLowerCase()
            .includes(termino) ?? false;

        const coincideEspecialidad =
          medico.especialidad
            ?.toLowerCase()
            .includes(termino) ?? false;

        return (
          coincideNombre ||
          coincideEspecialidad
        );
      });

    this.cdr.markForCheck();
  }

}