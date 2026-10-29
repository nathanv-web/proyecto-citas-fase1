import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router } from '@angular/router';

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
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.fechaMinima = this.obtenerFechaActual();

    const idMedico =
      this.route.snapshot.queryParamMap.get('idMedico');

    if (idMedico) {
      this.idMedicoSeleccionado = Number(idMedico);
    }

    this.cargarMedicos();
  }

  cargarMedicos(): void {

    this.loadingMedicos = true;
    this.errorMessage = '';

    this.medicoService.obtenerMedicos().subscribe({

      next: (medicos) => {
        this.medicos = medicos;
        this.loadingMedicos = false;

        this.cdr.markForCheck();
      },

      error: (error: HttpErrorResponse) => {
        this.loadingMedicos = false;
        this.errorMessage =
          'No fue posible cargar los médicos.';

        this.cdr.markForCheck();
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

      this.cdr.markForCheck();
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

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {
          this.loadingHorarios = false;
          this.busquedaRealizada = true;
          this.errorMessage =
            'No fue posible cargar los horarios disponibles.';

          this.cdr.markForCheck();
        }

      });
  }

  seleccionarHorario(
    horario: HorarioDisponible
  ): void {

    this.router.navigate(
      ['/confirm-appointment'],
      {
        queryParams: {
          idHorario: horario.idHorario,
          medico: horario.medico,
          fecha: horario.fecha,
          horaInicio: horario.horaInicio,
          horaFin: horario.horaFin
        }
      }
    );
  }

  get textoMedicoSeleccionado(): string {
  const medico = this.medicos.find(
    m => m.idMedico === this.idMedicoSeleccionado
  );

  if (!medico) {
    return '';
  }

  return `${medico.nombre} - ${medico.especialidad}`;
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