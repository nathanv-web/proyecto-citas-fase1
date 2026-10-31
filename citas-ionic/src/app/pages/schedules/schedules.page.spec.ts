import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { afterEach, vi } from 'vitest';
import { SchedulesPage } from './schedules.page';
import { MedicoService } from '../../core/services/medico';
import { HorarioDisponibleService } from '../../core/services/horario-disponible';

describe('SchedulesPage', () => {
  let component: SchedulesPage;
  let fixture: ComponentFixture<SchedulesPage>;

  beforeEach(() => {
    vi.useFakeTimers();
    // Las 15:00 en Guatemala, independientemente de la zona del dispositivo.
    vi.setSystemTime(new Date('2026-10-30T21:00:00Z'));
    TestBed.configureTestingModule({ providers: [
      provideRouter([]),
      { provide: MedicoService, useValue: { obtenerMedicos: () => of([]) } },
      { provide: HorarioDisponibleService, useValue: {
        obtenerHorariosDisponibles: (_id: number, fecha: string) => of(
          (fecha === '2026-10-31' ? ['09:00:00'] : ['09:00:00', '15:00:00', '15:01:00'])
            .map((horaInicio, i) => ({
              idHorario: i + 1, idMedico: 1, medico: 'Médico de prueba',
              fecha, horaInicio, horaFin: '16:00:00', estado: 'DISPONIBLE',
            })),
        ),
      } },
    ] });
    fixture = TestBed.createComponent(SchedulesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    fixture.destroy();
    vi.useRealTimers();
  });

  it('muestra solo horarios futuros y retira los que vencen con la vista abierta', () => {
    expect(component).toBeTruthy();
    component.idMedicoSeleccionado = 1;
    component.fechaSeleccionada = '2026-10-30';
    component.buscarHorarios();
    component.ionViewWillEnter();
    expect(component.horarios.map(h => h.horaInicio)).toEqual(['15:01:00']);

    const horario = component.horarios[0];
    vi.advanceTimersByTime(60_000);
    expect(component.horarios).toEqual([]);
    const navigate = vi.spyOn(TestBed.inject(Router), 'navigate');
    component.seleccionarHorario(horario);
    expect(navigate).not.toHaveBeenCalled();

    component.fechaSeleccionada = '2026-10-31';
    component.buscarHorarios();
    expect(component.horarios.map(h => h.horaInicio)).toEqual(['09:00:00']);
  });
});
