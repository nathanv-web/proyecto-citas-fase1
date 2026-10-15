import { Component } from '@angular/core';

import { PanelMenu } from '../../../shared/panel-menu/panel-menu';

/** El panel médico presenta sus funciones mediante el menú compartido. */
@Component({
  selector: 'app-doctor-dashboard',
  standalone: true,
  imports: [PanelMenu],
  template: ` <app-panel-menu panel="doctor" /> `,
})
export class DoctorDashboard {}
