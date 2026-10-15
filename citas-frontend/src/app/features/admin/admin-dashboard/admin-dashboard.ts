import { Component } from '@angular/core';
import { PanelMenu } from '../../../shared/panel-menu/panel-menu';

/** El panel administrativo utiliza el menú común para presentar sus funciones. */
@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [PanelMenu],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard {}
