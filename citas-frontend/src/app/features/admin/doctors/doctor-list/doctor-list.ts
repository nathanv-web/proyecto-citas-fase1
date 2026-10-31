import { ConfirmationService } from '../../../../core/notifications/confirmation.service';
import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { apiErrorMessage } from '../../../../core/notifications/api-error';

import { Doctor, DoctorRequest, DoctorUpdateRequest } from '../models/doctor.model';
import { DoctorAdminService } from '../services/doctor.service';
import { Specialty } from '../../specialties/models/specialty.model';
import { SpecialtyService } from '../../specialties/services/specialty.service';
import { User } from '../../users/models/user.model';
import { UserService } from '../../users/services/user.service';
import { finalize, Observable } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';

/** Administra perfiles médicos, incluidos su creación, edición y estado. */
@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './doctor-list.html',
  styleUrl: './doctor-list.css',
})
export class DoctorList implements OnInit {
  private readonly confirmation = inject(ConfirmationService);
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly formBuilder = inject(FormBuilder);
  readonly isCreate = inject(ActivatedRoute).snapshot.data['createDoctor'] === true;
  readonly canViewDoctors = !this.isCreate && this.authService.hasPermission('VER_MEDICOS');
  readonly canOpenList = this.authService.hasPermission('VER_MEDICOS');
  readonly canOpenCreate = ['CREAR_MEDICOS', 'VER_USUARIOS', 'VER_ESPECIALIDADES'].every(
    (permission) => this.authService.hasPermission(permission),
  );
  readonly canActivateDoctor = this.authService.hasPermission('ACTUALIZAR_MEDICOS');
  readonly canEditDoctor = !this.isCreate && this.canActivateDoctor;
  readonly canDeactivateDoctor = this.authService.hasPermission('ELIMINAR_MEDICOS');
  readonly canCreateDoctor = this.isCreate && this.canOpenCreate;

  private readonly doctorService = inject(DoctorAdminService);
  private readonly specialtyService = inject(SpecialtyService);
  private readonly userService = inject(UserService);

  readonly doctorForm = this.formBuilder.nonNullable.group({
    idUsuario: [0, [Validators.min(1)]],
    idEspecialidad: [0, [Validators.min(1)]],
    colegiado: ['', [Validators.required, Validators.pattern(/^COL-[0-9]{4}$/)]],
    aniosExperiencia: [0, [Validators.min(0)]],
    biografia: ['', [Validators.maxLength(1000)]],
  });

  readonly doctorEditForm = this.formBuilder.nonNullable.group({
    idEspecialidad: [0, [Validators.min(1)]],
    colegiado: ['', [Validators.required, Validators.pattern(/^COL-[0-9]{4}$/)]],
    aniosExperiencia: [0, [Validators.min(0)]],
    biografia: ['', [Validators.maxLength(1000)]],
  });

  doctors: Doctor[] = [];
  specialties: Specialty[] = [];
  users: User[] = [];
  editingDoctorId: number | null = null;

  loading = false;
  saving = false;
  updating = false;
  changingState = false;
  error = '';
  success = '';

  ngOnInit(): void {
    this.loadDoctors();
    if (this.canCreateDoctor || this.canEditDoctor) {
      this.loadSupportData();
    }
  }

  loadDoctors(): void {
    if (!this.canViewDoctors) {
      return;
    }
    this.loading = true;
    this.error = '';
    this.doctorService
      .getAll()
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (data) => {
          this.doctors = data;
          this.loading = false;
        },
        error: (error) => {
          this.error = apiErrorMessage(error);
          this.loading = false;
        },
      });
  }

  async changeDoctorState(doctor: Doctor): Promise<void> {
    const activate = doctor.estado === 'INACTIVO';
    if (
      !this.canViewDoctors ||
      this.changingState ||
      !(activate ? this.canActivateDoctor : this.canDeactivateDoctor) ||
      !['ACTIVO', 'INACTIVO'].includes(doctor.estado)
    ) {
      return;
    }
    if (
      !(await this.confirmation.confirm(
        `¿Deseas ${activate ? 'activar' : 'desactivar'} al médico ${doctor.nombre}?`,
      ))
    ) {
      return;
    }
    this.error = '';
    this.success = '';
    this.changingState = true;
    const request: Observable<unknown> = activate
      ? this.doctorService.activate(doctor.idMedico)
      : this.doctorService.deactivate(doctor.idMedico);
    request
      .pipe(
        finalize(() => {
          this.changingState = false;
          this.changeDetector.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.success = `Médico ${activate ? 'activado' : 'desactivado'} correctamente.`;
          this.loadDoctors();
        },
        error: (error) => {
          this.error = apiErrorMessage(error);
        },
      });
  }

  loadSupportData(): void {
    this.specialtyService
      .getAll()
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (data) => {
          this.specialties = data;
        },
        error: (error) => {
          this.error = apiErrorMessage(error);
        },
      });

    if (this.canCreateDoctor) {
      this.userService
        .getAll()
        .pipe(finalize(() => this.changeDetector.markForCheck()))
        .subscribe({
          next: (data) => {
            this.users = data.filter((user) => user.activo);
          },
          error: (error) => {
            this.error = apiErrorMessage(error);
          },
        });
    }
  }

  createDoctor(): void {
    if (!this.canCreateDoctor) {
      return;
    }
    this.error = '';
    this.success = '';
    if (this.doctorForm.invalid) {
      this.doctorForm.markAllAsTouched();
      this.error = 'Revisa los datos del perfil médico.';
      return;
    }

    const values = this.doctorForm.getRawValue();
    const request: DoctorRequest = {
      idUsuario: values.idUsuario,
      idEspecialidad: values.idEspecialidad,
      colegiado: values.colegiado.trim(),
      aniosExperiencia: values.aniosExperiencia,
      biografia: values.biografia.trim(),
    };
    this.saving = true;
    this.doctorService
      .create(request)
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          this.success = 'Médico creado correctamente.';
          this.doctorForm.reset({
            idUsuario: 0,
            idEspecialidad: 0,
            colegiado: '',
            aniosExperiencia: 0,
            biografia: '',
          });
          this.saving = false;
        },
        error: (error) => {
          this.error = apiErrorMessage(error);
          this.saving = false;
        },
      });
  }

  startEditDoctor(doctor: Doctor): void {
    if (!this.canEditDoctor) {
      return;
    }
    this.error = '';
    this.success = '';
    this.editingDoctorId = doctor.idMedico;
    const specialty = this.specialties.find(
      (item) => item.nombre.trim().toLocaleLowerCase() === doctor.especialidad.trim().toLocaleLowerCase(),
    );
    this.doctorEditForm.reset({
      idEspecialidad: specialty?.idEspecialidad ?? 0,
      colegiado: doctor.colegiado,
      aniosExperiencia: doctor.aniosExperiencia,
      biografia: doctor.biografia ?? '',
    });
  }

  cancelEditDoctor(): void {
    this.editingDoctorId = null;
    this.doctorEditForm.reset({ idEspecialidad: 0, colegiado: '', aniosExperiencia: 0, biografia: '' });
  }

  updateDoctor(): void {
    const id = this.editingDoctorId;
    if (!this.canEditDoctor || id === null || this.updating) {
      return;
    }
    if (this.doctorEditForm.invalid) {
      this.doctorEditForm.markAllAsTouched();
      this.error = 'Revisa los datos del perfil médico.';
      return;
    }

    const values = this.doctorEditForm.getRawValue();
    const request: DoctorUpdateRequest = {
      idEspecialidad: values.idEspecialidad,
      colegiado: values.colegiado.trim(),
      aniosExperiencia: values.aniosExperiencia,
      biografia: values.biografia.trim(),
    };
    this.error = '';
    this.success = '';
    this.updating = true;
    this.doctorService
      .update(id, request)
      .pipe(
        finalize(() => {
          this.updating = false;
          this.changeDetector.markForCheck();
        }),
      )
      .subscribe({
        next: () => {
          this.success = 'Perfil médico actualizado correctamente.';
          this.cancelEditDoctor();
          this.loadDoctors();
        },
        error: (error) => {
          this.error = apiErrorMessage(error);
        },
      });
  }
}
