import { Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';

import { Medico } from '../../core/models/medico';
import { HorarioDisponible } from '../../core/models/horario-disponible';

import { MedicoService } from '../../core/services/medico';
import { HorarioDisponibleService } from '../../core/services/horario-disponible';

@Component({
  selector: 'app-schedules',
  templateUrl: './schedules.page.html',
  styleUrls: ['./schedules.page.scss'],
  standalone: false,
})
export class SchedulesPage implements OnInit {

  medicos: Medico[] = [];
  horarios: HorarioDisponible[] = [];

  idMedicoSeleccionado: number | null = null;
  fechaSeleccionada = '';

  loadingMedicos = false;
  loadingHorarios = false;

  errorMessage = '';
  busquedaRealizada = false;

  fechaMinima = '';

  constructor(
    private medicoService: MedicoService,
    private horarioService: HorarioDisponibleService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.fechaMinima = this.obtenerFechaActual();
    this.cargarMedicos();

    const idMedico =
      this.route.snapshot.queryParamMap.get('idMedico');

    if (idMedico) {
      this.idMedicoSeleccionado = Number(idMedico);
    }
  }

  cargarMedicos(): void {

    this.loadingMedicos = true;
    this.errorMessage = '';

    this.medicoService.obtenerMedicos().subscribe({
      next: (medicos) => {
        this.medicos = medicos;
        this.loadingMedicos = false;
      },

      error: (error: HttpErrorResponse) => {
        this.loadingMedicos = false;

        this.errorMessage =
          error.error?.message ||
          'No fue posible cargar los médicos.';
      }
    });
  }

  buscarHorarios(): void {

    this.errorMessage = '';
    this.horarios = [];
    this.busquedaRealizada = false;

    if (
      this.idMedicoSeleccionado === null ||
      !this.fechaSeleccionada
    ) {
      this.errorMessage =
        'Seleccioná un médico y una fecha.';
      return;
    }

    this.loadingHorarios = true;

    this.horarioService
      .obtenerHorariosDisponibles(
        this.idMedicoSeleccionado,
        this.fechaSeleccionada
      )
      .subscribe({
        next: (horarios) => {
          this.horarios = horarios;
          this.loadingHorarios = false;
          this.busquedaRealizada = true;
        },

        error: (error: HttpErrorResponse) => {
          this.loadingHorarios = false;
          this.busquedaRealizada = true;

          this.errorMessage =
            error.error?.message ||
            'No fue posible consultar los horarios disponibles.';
        }
      });
  }

  private obtenerFechaActual(): string {

    const hoy = new Date();

    const anio = hoy.getFullYear();

    const mes = String(
      hoy.getMonth() + 1
    ).padStart(2, '0');

    const dia = String(
      hoy.getDate()
    ).padStart(2, '0');

    return `${anio}-${mes}-${dia}`;
  }
}
