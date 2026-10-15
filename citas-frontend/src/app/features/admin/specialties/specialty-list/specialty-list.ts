import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Specialty } from '../models/specialty.model';
import { SpecialtyService } from '../services/specialty.service';
import { AuthService } from '../../../../core/auth/services/auth.service';

import { finalize, Observable } from 'rxjs';

/** El componente presenta las especialidades y permite crearlas o cambiar su estado según los permisos. */
@Component({
  selector: 'app-specialty-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './specialty-list.html',
  styleUrl: './specialty-list.css',
})
export class SpecialtyList implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  readonly canViewSpecialties = this.authService.hasPermission('VER_ESPECIALIDADES');
  readonly canCreateSpecialty = this.authService.hasPermission('CREAR_ESPECIALIDADES');
  readonly canActivateSpecialty = this.authService.hasPermission('ACTUALIZAR_ESPECIALIDADES');
  readonly canDeactivateSpecialty = this.authService.hasPermission('ELIMINAR_ESPECIALIDADES');

  private readonly specialtyService = inject(SpecialtyService);

  specialties: Specialty[] = [];

  loading = false;
  saving = false;
  changingState = false;

  error = '';
  success = '';

  form: Specialty = {
    nombre: '',
    descripcion: '',
  };

  ngOnInit(): void {
    this.loadSpecialties();
  }

  loadSpecialties(): void {
    if (!this.canViewSpecialties) {
      return;
    }

    this.loading = true;
    this.error = '';

    this.specialtyService
      .getAll()
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (data) => {
          this.specialties = data;
          this.loading = false;
        },

        error: (error) => {
          this.error = error?.error?.message || 'No se pudieron cargar las especialidades.';

          this.loading = false;
        },
      });
  }

  changeSpecialtyState(specialty: Specialty): void {
    const activate = specialty.activo === false;
    if (
      !this.canViewSpecialties ||
      this.changingState ||
      !specialty.idEspecialidad ||
      !(activate ? this.canActivateSpecialty : this.canDeactivateSpecialty)
    ) {
      return;
    }
    if (
      !confirm(
        `¿Deseas ${activate ? 'activar' : 'desactivar'} la especialidad ${specialty.nombre}?`,
      )
    ) {
      return;
    }
    this.error = '';
    this.success = '';
    this.changingState = true;
    const request: Observable<unknown> = activate
      ? this.specialtyService.activate(specialty.idEspecialidad)
      : this.specialtyService.deactivate(specialty.idEspecialidad);
    request
      .pipe(
        finalize(() => {
          this.changingState = false;
          this.changeDetector.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.success = `Especialidad ${activate ? 'activada' : 'desactivada'} correctamente.`;
          this.loadSpecialties();
        },
        error: (error) => {
          this.error =
            error?.error?.message ||
            `No se pudo ${activate ? 'activar' : 'desactivar'} la especialidad.`;
        },
      });
  }

  createSpecialty(): void {
    if (!this.canCreateSpecialty) {
      return;
    }

    this.error = '';
    this.success = '';

    if (!this.form.nombre.trim()) {
      this.error = 'El nombre de la especialidad es obligatorio.';
      return;
    }

    this.saving = true;

    this.specialtyService
      .create({
        nombre: this.form.nombre.trim(),
        descripcion: this.form.descripcion.trim(),
      })
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          this.success = 'Especialidad creada correctamente.';

          this.form = {
            nombre: '',
            descripcion: '',
          };

          this.saving = false;

          this.loadSpecialties();
        },

        error: (error) => {
          this.error = error?.error?.message || 'No se pudo crear la especialidad.';

          this.saving = false;
        },
      });
  }
}
