import { Component, ElementRef, effect, inject, viewChild } from '@angular/core';
import { ConfirmationService } from './confirmation.service';

@Component({
  selector: 'app-confirmation-dialog',
  standalone: true,
  template: `<dialog #dialog aria-labelledby="confirmation-title" (cancel)="cancel($event)">
    <h2 id="confirmation-title">Confirmar acción</h2>
    <p>{{ service.message() }}</p>
    <form method="dialog">
      <button type="submit" autofocus (click)="service.answer(false)">Volver</button>
      <button type="submit" (click)="service.answer(true)">Confirmar</button>
    </form>
  </dialog>`,
  styles: [
    `
      dialog {
        border: 0;
        border-radius: 1rem;
        padding: 1.5rem;
        max-width: min(28rem, 90vw);
        box-shadow: 0 10px 40px #0004;
      }
      dialog::backdrop {
        background: #12273788;
      }
      form {
        display: flex;
        gap: 1rem;
        justify-content: end;
      }
      button {
        padding: 0.6rem 1rem;
        cursor: pointer;
      }
    `,
  ],
})
export class ConfirmationDialog {
  readonly service = inject(ConfirmationService);
  readonly dialog = viewChild<ElementRef<HTMLDialogElement>>('dialog');
  constructor() {
    effect(() => {
      const element = this.dialog()?.nativeElement;
      if (this.service.message()) {
        if (element && !element.open) element.showModal();
      } else element?.close();
    });
  }
  cancel(event: Event): void {
    event.preventDefault();
    this.service.answer(false);
  }
}
