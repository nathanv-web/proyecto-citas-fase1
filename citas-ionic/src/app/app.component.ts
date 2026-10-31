import { Component, HostListener, OnDestroy } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: 'app.component.html',
  styleUrls: ['app.component.scss'],
  standalone: false,
})
export class AppComponent implements OnDestroy {
  connectionStatus: 'online' | 'offline' | 'restored' = 'online';
  private restoredNoticeTimer?: ReturnType<typeof setTimeout>;

  constructor() {
    if (typeof navigator !== 'undefined' && !navigator.onLine) {
      this.connectionStatus = 'offline';
    }
  }

  @HostListener('window:offline')
  onOffline(): void {
    this.clearRestoredNoticeTimer();
    this.connectionStatus = 'offline';
  }

  @HostListener('window:online')
  onOnline(): void {
    this.clearRestoredNoticeTimer();
    this.connectionStatus = 'restored';
    this.restoredNoticeTimer = setTimeout(() => {
      this.connectionStatus = 'online';
      this.restoredNoticeTimer = undefined;
    }, 5000);
  }

  ngOnDestroy(): void {
    this.clearRestoredNoticeTimer();
  }

  private clearRestoredNoticeTimer(): void {
    if (this.restoredNoticeTimer) {
      clearTimeout(this.restoredNoticeTimer);
      this.restoredNoticeTimer = undefined;
    }
  }
}
