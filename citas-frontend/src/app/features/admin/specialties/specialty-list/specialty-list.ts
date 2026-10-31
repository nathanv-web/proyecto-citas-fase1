import { ConfirmationService } from '../../../../core/notifications/confirmation.service';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormsModule, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { apiErrorMessage } from '../../../../core/notifications/api-error';

import { Specialty } from '../models/specialty.model';
import { SpecialtyService } from '../services/specialty.service';
import { AuthService } from '../../../../core/auth/services/auth.service';

import { finalize, Observable } from 'rxjs';

/** El componente presenta las especialidades y permite crearlas o cambiar su estado según los permisos. */
@Component({
  selector: 'app-specialty-list',
  standalone: true,
  imports: [FormsModule, ReactiveFormsModule, RouterLink],
  templateUrl: './specialty-list.html',
  styleUrl: './specialty-list.css',
})
export class SpecialtyList implements OnInit {
  private readonly confirmation = inject(ConfirmationService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);
  readonly isCreate = inject(ActivatedRoute).snapshot.data['createSpecialty'] === true;
  readonly canOpenList = this.authService.hasPermission('VER_ESPECIALIDADES');
  readonly canOpenCreate = this.authService.hasPermission('CREAR_ESPECIALIDADES');
  readonly canViewSpecialties = !this.isCreate && this.canOpenList;
  readonly canCreateSpecialty = this.isCreate && this.canOpenCreate;
  readonly canActivateSpecialty = this.authService.hasPermission('ACTUALIZAR_ESPECIALIDADES');
  readonly canEditSpecialty = !this.isCreate && this.canActivateSpecialty;
  readonly canDeactivateSpecialty = this.authService.hasPermission('ELIMINAR_ESPECIALIDADES');

  private readonly specialtyService = inject(SpecialtyService);

  specialties: Specialty[] = [];
  editingSpecialtyId: number | null = null;

  loading = false;
  saving = false;
  updating = false;
  changingState = false;

  error = '';
  success = '';

  form: Specialty = {
    nombre: '',
    descripcion: '',
  };

  readonly specialtyEditForm = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    descripcion: ['', [Validators.maxLength(255)]],
  });

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
          this.error = apiErrorMessage(error);
          this.loading = false;
        },
      });
  }

  async changeSpecialtyState(specialty: Specialty): Promise<void> {
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
      !(await this.confirmation.confirm(
        `¿Deseas ${activate ? 'activar' : 'desactivar'} la especialidad ${specialty.nombre}?`,
      ))
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
          this.error = apiErrorMessage(error);
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
          this.error = apiErrorMessage(error);
          this.saving = false;
        },
      });
  }

  startEditSpecialty(specialty: Specialty): void {
    if (!this.canEditSpecialty || !specialty.idEspecialidad) {
      return;
    }
    this.error = '';
    this.success = '';
    this.editingSpecialtyId = specialty.idEspecialidad;
    this.specialtyEditForm.reset({
      nombre: specialty.nombre,
      descripcion: specialty.descripcion ?? '',
    });
  }

  cancelEditSpecialty(): void {
    this.editingSpecialtyId = null;
    this.specialtyEditForm.reset({ nombre: '', descripcion: '' });
  }

  updateSpecialty(): void {
    const id = this.editingSpecialtyId;
    if (!this.canEditSpecialty || id === null || this.updating) {
      return;
    }
    if (this.specialtyEditForm.invalid) {
      this.specialtyEditForm.markAllAsTouched();
      this.error = 'El nombre de la especialidad es obligatorio.';
      return;
    }

    const values = this.specialtyEditForm.getRawValue();
    this.error = '';
    this.success = '';
    this.updating = true;
    this.specialtyService
      .update(id, { nombre: values.nombre.trim(), descripcion: values.descripcion.trim() })
      .pipe(
        finalize(() => {
          this.updating = false;
          this.changeDetector.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.success = 'Especialidad actualizada correctamente.';
          this.cancelEditSpecialty();
          this.loadSpecialties();
        },
        error: (error) => {
          this.error = apiErrorMessage(error);
        },
      });
  }
}
