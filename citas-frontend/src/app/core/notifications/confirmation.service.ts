import { Injectable, signal } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class ConfirmationService {
  readonly message = signal('');
  private resolve?: (confirmed: boolean) => void;

  confirm(message: string): Promise<boolean> {
    if (this.resolve) return Promise.resolve(false);
    this.message.set(message);
    return new Promise((resolve) => {
      this.resolve = resolve;
    });
  }

  answer(confirmed: boolean): void {
    this.resolve?.(confirmed);
    this.resolve = undefined;
    this.message.set('');
  }
}
