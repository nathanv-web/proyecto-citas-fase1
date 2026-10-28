import { Component, inject } from '@angular/core';
import { NotificationService } from './notification.service';

@Component({
  selector: 'app-toast-container',
  standalone: true,
  template: ` <section class="toasts" aria-label="Notificaciones">
    @for (item of service.notifications(); track item.id) {
      <div
        class="toast"
        [class]="'toast ' + item.type"
        [attr.role]="item.type === 'error' ? 'alert' : 'status'"
        (mouseenter)="service.pause(item.id)"
        (mouseleave)="service.resume(item.id)"
        (focusin)="service.pause(item.id)"
        (focusout)="service.resume(item.id)"
      >
        <span>{{ item.message }}</span>
        <button type="button" aria-label="Cerrar notificación" (click)="service.close(item.id)">
          ×
        </button>
      </div>
    }
  </section>`,
  styles: [
    `
      .toasts {
        position: fixed;
        top: 1rem;
        right: 1rem;
        z-index: 2000;
        width: min(26rem, calc(100vw - 2rem));
        display: grid;
        gap: 0.65rem;
      }
      .toast {
        display: flex;
        justify-content: space-between;
        align-items: start;
        gap: 1rem;
        padding: 1rem;
        border-radius: 0.75rem;
        background: #fff;
        color: #172b3a;
        border-left: 5px solid #2774a5;
        box-shadow: 0 5px 24px #0003;
        white-space: pre-line;
        overflow-wrap: anywhere;
      }
      .error {
        border-color: #bd3030;
      }
      .success {
        border-color: #198754;
      }
      .warning {
        border-color: #bd8300;
      }
      button {
        border: 0;
        background: transparent;
        font-size: 1.4rem;
        cursor: pointer;
        color: inherit;
      }
    `,
  ],
})
export class ToastContainer {
  readonly service = inject(NotificationService);
}
