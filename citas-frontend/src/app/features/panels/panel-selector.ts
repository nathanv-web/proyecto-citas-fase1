import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PanelAccessService } from '../../core/navigation/panel-access.service';

/** El selector muestra las áreas disponibles según los permisos de la cuenta. */
@Component({
  selector: 'app-panel-selector',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './panel-selector.html',
  styleUrl: '../../shared/panel-menu/panel-menu.css',
})
export class PanelSelector {
  private readonly access = inject(PanelAccessService);
  get panels() {
    return this.access.getPanels();
  }
}
