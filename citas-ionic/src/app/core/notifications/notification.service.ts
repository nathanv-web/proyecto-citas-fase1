import { Injectable } from '@angular/core';
import { ToastController } from '@ionic/angular';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private readonly visible = new Set<string>();
  constructor(private readonly controller: ToastController) {}

  async error(message: string): Promise<void> {
    if (this.visible.has(message)) return;
    this.visible.add(message);
    try {
      const toast = await this.controller.create({
        message, duration: 6000, position: 'top', color: 'danger',
        buttons: [{ text: 'Cerrar', role: 'cancel' }],
      });
      await toast.present();
      await toast.onDidDismiss();
    } finally { this.visible.delete(message); }
  }
}
