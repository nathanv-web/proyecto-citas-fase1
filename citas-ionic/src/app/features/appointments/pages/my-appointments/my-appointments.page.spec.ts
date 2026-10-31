import { ChangeDetectorRef } from '@angular/core';
import { Router } from '@angular/router';
import { of } from 'rxjs';
import { AppointmentService } from '../../services/appointment.service';
import { AppointmentResponse } from '../../models/appointment-response';
import { MyAppointmentsPage } from './my-appointments.page';

describe('MyAppointmentsPage', () => {
  const cita = (estado: string, idCita: number): AppointmentResponse => ({
    idCita,
    idMedico: 1,
    paciente: 'Paciente',
    medico: 'Médico',
    especialidad: 'General',
    fecha: '2026-10-30',
    horaInicio: '08:00',
    horaFin: '08:30',
    estado,
    motivo: 'Consulta',
    motivoCancelacion: null,
    diagnosticoReceta: null,
    observaciones: null,
  });

  function createPage(): MyAppointmentsPage {
    const appointments = [cita('PENDIENTE', 1), cita('COMPLETADA', 2), cita('CANCELADA', 3)];
    const service = { obtenerHistorial: () => of(appointments) } as unknown as AppointmentService;
    return new MyAppointmentsPage(service, {} as Router, { markForCheck: vi.fn() } as unknown as ChangeDetectorRef);
  }

  it('separa las citas pendientes de las atendidas y canceladas', () => {
    const page = createPage();
    page.cargarCitas();

    expect(page.citasPendientes.map((item) => item.idCita)).toEqual([1]);
    expect(page.citasHistorial.map((item) => item.idCita)).toEqual([2, 3]);
  });
});
