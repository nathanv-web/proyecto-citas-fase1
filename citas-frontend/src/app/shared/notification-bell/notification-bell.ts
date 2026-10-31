import { ChangeDetectorRef, Component, DestroyRef, ElementRef, OnInit, inject } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { finalize, timer } from 'rxjs';
import { NotificationItem, NotificationService } from '../../services/notification';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [],
  styleUrl: './notification-bell.css',
  host: {
    '(document:keydown.escape)': 'closeDropdown()',
    '(document:click)': 'closeOutside($event)',
    '(window:focus)': 'loadNotifications()',
  },
  template: `
    <div class="notification-container" style="position: relative; display: inline-block;">
      <button
        type="button"
        aria-label="Centro de avisos"
        [attr.aria-expanded]="isDropdownOpen"
        (click)="toggleDropdown()"
        style="background: none; border: none; cursor: pointer; position: relative;"
      >
        <span style="font-size: 24px;">🔔</span>
        @if (unreadCount > 0) {
          <span
            style="position: absolute; top: -5px; right: -5px; background: red; color: white; border-radius: 50%; padding: 2px 6px; font-size: 12px; font-weight: bold;"
          >
            {{ unreadCount }}
          </span>
        }
      </button>

      @if (isDropdownOpen) {
        <div class="notification-panel" role="region" aria-labelledby="notifications-title">
          <header class="notification-panel-header">
            <h2 id="notifications-title">Centro de avisos</h2>
            <button
              type="button"
              class="notification-close"
              aria-label="Cerrar avisos"
              (click)="closeDropdown()"
            >
              ×
            </button>
          </header>

          @if (isLoading) {
            <p style="text-align: center; color: #666;">Cargando avisos...</p>
          }

          @if (hasError) {
            <p style="text-align: center; color: red;">No se pudieron cargar los avisos.</p>
          }

          @if (!isLoading && !hasError && notifications.length === 0) {
            <p style="text-align: center; color: #666;">No tienes avisos nuevos.</p>
          }

          @if (!isLoading && !hasError && notifications.length > 0) {
            <ul
              class="notification-list"
              style="list-style: none; padding: 0; margin: 0; max-height: 250px; overflow-y: auto;"
            >
              @for (notif of notifications; track notif.idNotificacion) {
                <li
                  [style.background]="notif.leida ? 'white' : '#f0f8ff'"
                  style="padding: 10px; margin-bottom: 5px; border-radius: 4px; border: 1px solid #eee;"
                >
                  <strong>{{ notif.titulo }}</strong>
                  <p style="margin: 0 0 5px 0; font-size: 14px;">{{ notif.mensaje }}</p>
                  @if (!notif.leida) {
                    <button
                      type="button"
                      [disabled]="readingId !== null"
                      (click)="markAsRead(notif.idNotificacion)"
                      style="font-size: 12px; padding: 4px 8px; border: none; background: #007bff; color: white; border-radius: 4px; cursor: pointer;"
                    >
                      Marcar como leído
                    </button>
                  }
                </li>
              }
            </ul>
          }
        </div>
      }
    </div>
  `,
})
export class NotificationBellComponent implements OnInit {
  private service = inject(NotificationService);
  private changeDetector = inject(ChangeDetectorRef);
  private element = inject(ElementRef<HTMLElement>);
  private destroyRef = inject(DestroyRef);

  notifications: NotificationItem[] = [];
  readingId: number | null = null;
  unreadCount = 0;
  isDropdownOpen = false;
  isLoading = false;
  hasError = false;

  ngOnInit(): void {
    this.loadNotifications();
    timer(30000, 30000)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        if (!document.hidden) this.loadNotifications();
      });
  }

  loadNotifications(): void {
    if (this.isLoading) return;
    this.isLoading = true;
    this.hasError = false;

    this.service
      .getNotifications()
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => this.changeDetector.markForCheck()),
      )
      .subscribe({
        next: (data) => {
          this.notifications = Array.isArray(data) ? data : [];
          this.unreadCount = this.notifications.filter((n) => !n.leida).length;
          this.isLoading = false;
        },
        error: () => {
          this.hasError = true;
          this.isLoading = false;
        },
      });
  }

  markAsRead(id: number): void {
    if (this.readingId !== null) return;
    this.readingId = id;
    this.service
      .markAsRead(id)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => {
          this.readingId = null;
          this.changeDetector.markForCheck();
        }),
      )
      .subscribe({
        next: (updated) => {
          const notif = this.notifications.find((n) => n.idNotificacion === id);
          if (notif && !notif.leida) {
            Object.assign(notif, updated);
            this.unreadCount = Math.max(0, this.unreadCount - 1);
          }
        },
        error: () => {},
      });
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
    if (this.isDropdownOpen) this.loadNotifications();
  }

  closeDropdown(): void {
    this.isDropdownOpen = false;
  }

  closeOutside(event: Event): void {
    if (event.target instanceof Node && !this.element.nativeElement.contains(event.target)) {
      this.closeDropdown();
    }
  }
}

export { NotificationBellComponent as NotificationBell };
