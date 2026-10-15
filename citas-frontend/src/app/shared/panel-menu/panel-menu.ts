import { Component, inject, Input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PanelAccessService } from '../../core/navigation/panel-access.service';
import { PanelId } from '../../core/navigation/panels.config';

/** El menú presenta las funciones que la cuenta tiene autorizadas dentro del panel seleccionado. */
@Component({
  selector: 'app-panel-menu',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './panel-menu.html',
  styleUrl: './panel-menu.css',
})
export class PanelMenu {
  private readonly access = inject(PanelAccessService);
  @Input({ required: true }) panel!: PanelId;

  get definition() {
    return this.access.getDefinition(this.panel);
  }
  get functions() {
    return this.access.getFunctions(this.panel);
  }
}
