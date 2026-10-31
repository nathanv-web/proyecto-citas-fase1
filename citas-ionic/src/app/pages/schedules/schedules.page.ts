import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
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
export class SchedulesPage implements OnInit, OnDestroy {
  medicos: Medico[] = [];
  horarios: HorarioDisponible[] = [];

  idMedicoSeleccionado: number | null = null;
  fechaSeleccionada = '';

  loadingMedicos = false;
  loadingHorarios = false;

  errorMessage = '';
  busquedaRealizada = false;

  fechaMinima = '';
  private actualizacionHorarios?: ReturnType<typeof setInterval>;

  constructor(
    private medicoService: MedicoService,
    private horarioService: HorarioDisponibleService,
    private route: ActivatedRoute,
    private router: Router,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.fechaMinima = this.obtenerFechaActual();

    const idMedico = this.route.snapshot.queryParamMap.get('idMedico');

    if (idMedico) {
      this.idMedicoSeleccionado = Number(idMedico);
    }

    this.cargarMedicos();
  }

  ionViewWillEnter(): void {
    this.detenerActualizacion();
    this.retirarHorariosVencidos();
    this.actualizacionHorarios = setInterval(() => this.retirarHorariosVencidos(), 15_000);
    if (this.busquedaRealizada && !this.loadingHorarios) {
      this.buscarHorarios();
    }
  }

  ionViewWillLeave(): void {
    this.detenerActualizacion();
  }

  ngOnDestroy(): void {
    this.detenerActualizacion();
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

        if (error.status === 0) {
          this.errorMessage = 'No se pudo conectar con el servidor.';
        } else {
          this.errorMessage =
            error.error?.message ?? 'No fue posible cargar los médicos.';
        }

        this.cdr.markForCheck();
      },
    });
  }

  buscarHorarios(): void {
    this.errorMessage = '';
    this.horarios = [];
    this.busquedaRealizada = false;

    if (this.idMedicoSeleccionado === null || !this.fechaSeleccionada) {
      this.errorMessage = 'Seleccioná un médico y una fecha.';

      this.cdr.markForCheck();
      return;
    }

    this.loadingHorarios = true;
    this.cdr.markForCheck();

    this.horarioService
      .obtenerHorariosDisponibles(
        this.idMedicoSeleccionado,
        this.fechaSeleccionada,
      )
      .subscribe({
        next: (horarios) => {
          this.horarios = horarios.filter(horario => this.estaVigente(horario));
          this.loadingHorarios = false;
          this.busquedaRealizada = true;

          this.cdr.markForCheck();
        },

        error: (error: HttpErrorResponse) => {
          this.loadingHorarios = false;
          this.busquedaRealizada = true;

          if (error.status === 0) {
            this.errorMessage = 'No se pudo conectar con el servidor.';
          } else {
            this.errorMessage =
              error.error?.message ??
              'No fue posible consultar los horarios disponibles.';
          }

          this.cdr.markForCheck();
        },
      });
  }

  seleccionarHorario(horario: HorarioDisponible): void {
    if (!this.estaVigente(horario)) {
      this.retirarHorariosVencidos();
      this.errorMessage = 'Ese horario ya pasó. Seleccioná otro horario disponible.';
      this.cdr.markForCheck();
      return;
    }

    const medicoEncontrado = this.medicos.find(
      (medico) => medico.idMedico === this.idMedicoSeleccionado,
    );

    this.router.navigate(['/confirm-appointment'], {
      queryParams: {
        idHorario: horario.idHorario,

        medico: medicoEncontrado?.nombre ?? horario.medico ?? '',

        fecha: horario.fecha ?? this.fechaSeleccionada,

        horaInicio: horario.horaInicio,

        horaFin: horario.horaFin,
      },
    });
  }

  get textoMedicoSeleccionado(): string {
    const medico = this.medicos.find(
      (medico) => medico.idMedico === this.idMedicoSeleccionado,
    );

    if (!medico) {
      return '';
    }

    return `${medico.nombre} - ${medico.especialidad}`;
  }

  private obtenerFechaActual(): string {
    return new Intl.DateTimeFormat('en-CA', {
      timeZone: 'America/Guatemala', year: 'numeric', month: '2-digit', day: '2-digit',
    }).format(new Date());
  }

  private estaVigente(horario: HorarioDisponible): boolean {
    // Guatemala usa UTC-06:00; la comparación no depende de la zona del teléfono.
    const inicio = Date.parse(`${horario.fecha}T${horario.horaInicio}-06:00`);
    return horario.estado === 'DISPONIBLE' && inicio > Date.now();
  }

  private retirarHorariosVencidos(): void {
    this.fechaMinima = this.obtenerFechaActual();
    const vigentes = this.horarios.filter(horario => this.estaVigente(horario));
    if (vigentes.length !== this.horarios.length) {
      this.horarios = vigentes;
    }
    this.cdr.markForCheck();
  }

  private detenerActualizacion(): void {
    if (this.actualizacionHorarios !== undefined) {
      clearInterval(this.actualizacionHorarios);
      this.actualizacionHorarios = undefined;
    }
  }
}
