import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import {
  Doctor,
  DoctorRequest
} from '../models/doctor.model';

import {
  DoctorAdminService
} from '../services/doctor.service';

import {
  Specialty
} from '../../specialties/models/specialty.model';

import {
  SpecialtyService
} from '../../specialties/services/specialty.service';

import {
  User
} from '../../users/models/user.model';

import {
  UserService
} from '../../users/services/user.service';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './doctor-list.html',
  styleUrl: './doctor-list.css'
})
export class DoctorList implements OnInit {

  private readonly doctorService =
    inject(DoctorAdminService);

  private readonly specialtyService =
    inject(SpecialtyService);

  private readonly userService =
    inject(UserService);

  doctors: Doctor[] = [];
  specialties: Specialty[] = [];
  users: User[] = [];

  loading = false;
  saving = false;

  error = '';
  success = '';

  form: DoctorRequest = {
    idUsuario: 0,
    idEspecialidad: 0,
    colegiado: '',
    aniosExperiencia: 0,
    biografia: ''
  };

  ngOnInit(): void {
    this.loadDoctors();
    this.loadSupportData();
  }

  loadDoctors(): void {

    this.loading = true;
    this.error = '';

    this.doctorService.getAll().subscribe({
      next: (data) => {
        this.doctors = data;
        this.loading = false;
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudieron cargar los médicos.';

        this.loading = false;
      }
    });
  }

  loadSupportData(): void {

    this.specialtyService.getAll().subscribe({
      next: (data) => {
        this.specialties = data;
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudieron cargar las especialidades.';
      }
    });

    this.userService.getAll().subscribe({
      next: (data) => {
        this.users = data.filter(
          user => user.activo
        );
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudieron cargar los usuarios.';
      }
    });
  }

  createDoctor(): void {

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
      this.error =
        'El número de colegiado es obligatorio.';
      return;
    }

    if (this.form.aniosExperiencia < 0) {
      this.error =
        'Los años de experiencia no pueden ser negativos.';
      return;
    }

    this.saving = true;

    this.doctorService.create({
      idUsuario: this.form.idUsuario,
      idEspecialidad: this.form.idEspecialidad,
      colegiado: this.form.colegiado.trim(),
      aniosExperiencia:
        this.form.aniosExperiencia,
      biografia:
        this.form.biografia.trim()
    }).subscribe({
      next: () => {

        this.success =
          'Médico creado correctamente.';

        this.form = {
          idUsuario: 0,
          idEspecialidad: 0,
          colegiado: '',
          aniosExperiencia: 0,
          biografia: ''
        };

        this.saving = false;

        this.loadDoctors();
      },

      error: (error) => {

        this.error =
          error?.error?.message ||
          'No se pudo crear el médico.';

        this.saving = false;
      }
    });
  }
}
