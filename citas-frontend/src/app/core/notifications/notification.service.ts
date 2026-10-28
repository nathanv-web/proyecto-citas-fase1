import { Injectable, signal } from '@angular/core';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';
export interface Notification {
  id: number;
  type: NotificationType;
  message: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationService {
  readonly notifications = signal<Notification[]>([]);
  private nextId = 0;
  private timers = new Map<number, ReturnType<typeof setTimeout>>();

  success(message: string): void {
    this.show('success', message);
  }
  error(message: string): void {
    this.show('error', message);
  }
  warning(message: string): void {
    this.show('warning', message);
  }
  info(message: string): void {
    this.show('info', message);
  }

  close(id: number): void {
    clearTimeout(this.timers.get(id));
    this.timers.delete(id);
    this.notifications.update((items) => items.filter((item) => item.id !== id));
  }

  pause(id: number): void {
    clearTimeout(this.timers.get(id));
  }
  resume(id: number): void {
    this.pause(id);
    this.timers.set(
      id,
      setTimeout(() => this.close(id), 6000),
    );
  }

  private show(type: NotificationType, message: string): void {
    if (
      !message ||
      this.notifications().some((item) => item.type === type && item.message === message)
    )
      return;
    if (this.notifications().length >= 4) this.close(this.notifications()[0].id);
    const id = ++this.nextId;
    this.notifications.update((items) => [...items, { id, type, message }]);
    this.resume(id);
  }
}
