import { Component, inject, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { Specialty } from '../models/specialty.model';
import { SpecialtyService } from '../services/specialty.service';

@Component({
  selector: 'app-specialty-list',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './specialty-list.html',
  styleUrl: './specialty-list.css'
})
export class SpecialtyList implements OnInit {

  private readonly specialtyService =
    inject(SpecialtyService);

  specialties: Specialty[] = [];

  loading = false;
  saving = false;

  error = '';
  success = '';

  form: Specialty = {
    nombre: '',
    descripcion: ''
  };

  ngOnInit(): void {
    this.loadSpecialties();
  }

  loadSpecialties(): void {

    this.loading = true;
    this.error = '';

    this.specialtyService.getAll().subscribe({
      next: (data) => {
        this.specialties = data;
        this.loading = false;
      },

      error: (error) => {
        this.error =
          error?.error?.message ||
          'No se pudieron cargar las especialidades.';

        this.loading = false;
      }
    });
  }

  createSpecialty(): void {

    this.error = '';
    this.success = '';

    if (!this.form.nombre.trim()) {
      this.error =
        'El nombre de la especialidad es obligatorio.';
      return;
    }

    this.saving = true;

    this.specialtyService.create({
      nombre: this.form.nombre.trim(),
      descripcion: this.form.descripcion.trim()
    }).subscribe({
      next: () => {

        this.success =
          'Especialidad creada correctamente.';

        this.form = {
          nombre: '',
          descripcion: ''
        };

        this.saving = false;

        this.loadSpecialties();
      },

      error: (error) => {

        this.error =
          error?.error?.message ||
          'No se pudo crear la especialidad.';

        this.saving = false;
      }
    });
  }
}
