import { Component } from '@angular/core';

import { PanelMenu } from '../../../shared/panel-menu/panel-menu';

/** El panel del paciente presenta las funciones de consulta y reserva autorizadas. */
@Component({
  selector: 'app-patient-dashboard',
  standalone: true,
  imports: [PanelMenu],
  template: ` <app-panel-menu panel="patient" /> `,
})
export class PatientDashboard {}
