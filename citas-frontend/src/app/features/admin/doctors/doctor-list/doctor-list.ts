import { ChangeDetectorRef, Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Doctor, DoctorRequest } from '../models/doctor.model';

import { DoctorAdminService } from '../services/doctor.service';

import { Specialty } from '../../specialties/models/specialty.model';

import { SpecialtyService } from '../../specialties/services/specialty.service';

import { User } from '../../users/models/user.model';

import { UserService } from '../../users/services/user.service';

import { finalize, Observable } from 'rxjs';
import { AuthService } from '../../../../core/auth/services/auth.service';

/** El componente administra los perfiles médicos y carga los catálogos necesarios para su creación. */
@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './doctor-list.html',
  styleUrl: './doctor-list.css',
})
export class DoctorList implements OnInit {
  private readonly changeDetector = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  readonly canViewDoctors = this.authService.hasPermission('VER_MEDICOS');
  readonly canActivateDoctor = this.authService.hasPermission('ACTUALIZAR_MEDICOS');
  readonly canDeactivateDoctor = this.authService.hasPermission('ELIMINAR_MEDICOS');

  readonly canCreateDoctor = ['CREAR_MEDICOS', 'VER_USUARIOS', 'VER_ESPECIALIDADES'].every(
    (permission) => this.authService.hasPermission(permission),
  );

  private readonly doctorService = inject(DoctorAdminService);

  private readonly specialtyService = inject(SpecialtyService);

  private readonly userService = inject(UserService);

  doctors: Doctor[] = [];
  specialties: Specialty[] = [];
  users: User[] = [];

  loading = false;
  saving = false;
  changingState = false;

  error = '';
  success = '';

  form: DoctorRequest = {
    idUsuario: 0,
    idEspecialidad: 0,
    colegiado: '',
    aniosExperiencia: 0,
    biografia: '',
  };

  ngOnInit(): void {
    this.loadDoctors();
    if (this.canCreateDoctor) {
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
          this.error = error?.error?.message || 'No se pudieron cargar los médicos.';

          this.loading = false;
        },
      });
  }

  changeDoctorState(doctor: Doctor): void {
    const activate = doctor.estado === 'INACTIVO';
    if (
      !this.canViewDoctors ||
      this.changingState ||
      !(activate ? this.canActivateDoctor : this.canDeactivateDoctor) ||
      !['ACTIVO', 'INACTIVO'].includes(doctor.estado)
    ) {
      return;
    }
    if (!confirm(`¿Deseas ${activate ? 'activar' : 'desactivar'} al médico ${doctor.nombre}?`)) {
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
          this.error =
            error?.error?.message || `No se pudo ${activate ? 'activar' : 'desactivar'} el médico.`;
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
          this.error = error?.error?.message || 'No se pudieron cargar las especialidades.';
        },
      });

    this.userService
      .getAll()
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: (data) => {
          this.users = data.filter((user) => user.activo);
        },

        error: (error) => {
          this.error = error?.error?.message || 'No se pudieron cargar los usuarios.';
        },
      });
  }

  createDoctor(): void {
    if (!this.canCreateDoctor) {
      return;
    }

    this.error = '';
    this.success = '';

    if (!this.form.idUsuario) {
      this.error = 'Selecciona un usuario.';
      return;
    }

    if (!this.form.idEspecialidad) {
      this.error = 'Selecciona una especialidad.';
      return;
    }

    if (!this.form.colegiado.trim()) {
      this.error = 'El número de colegiado es obligatorio.';
      return;
    }

    if (this.form.aniosExperiencia < 0) {
      this.error = 'Los años de experiencia no pueden ser negativos.';
      return;
    }

    this.saving = true;

    this.doctorService
      .create({
        idUsuario: this.form.idUsuario,
        idEspecialidad: this.form.idEspecialidad,
        colegiado: this.form.colegiado.trim(),
        aniosExperiencia: this.form.aniosExperiencia,
        biografia: this.form.biografia.trim(),
      })
      .pipe(finalize(() => this.changeDetector.markForCheck()))
      .subscribe({
        next: () => {
          this.success = 'Médico creado correctamente.';

          this.form = {
            idUsuario: 0,
            idEspecialidad: 0,
            colegiado: '',
            aniosExperiencia: 0,
            biografia: '',
          };

          this.saving = false;

          this.loadDoctors();
        },

        error: (error) => {
          this.error = error?.error?.message || 'No se pudo crear el médico.';

          this.saving = false;
        },
      });
  }
}
