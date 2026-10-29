import { Component, OnInit, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [],
  template: `
    <div class="notification-container" style="position: relative; display: inline-block;">
      <button type="button" (click)="toggleDropdown()" style="background: none; border: none; cursor: pointer; position: relative;">
        <span style="font-size: 24px;">🔔</span>
        @if (unreadCount > 0) {
          <span style="position: absolute; top: -5px; right: -5px; background: red; color: white; border-radius: 50%; padding: 2px 6px; font-size: 12px; font-weight: bold;">
            {{ unreadCount }}
          </span>
        }
      </button>

      @if (isDropdownOpen) {
        <div style="position: absolute; right: 0; top: 40px; width: 280px; background: white; color: #222; border: 1px solid #ccc; border-radius: 8px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); z-index: 1000; padding: 10px;">
          <h4 style="margin: 0 0 10px 0; padding-bottom: 5px; border-bottom: 1px solid #eee;">Centro de Avisos</h4>

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
            <ul style="list-style: none; padding: 0; margin: 0; max-height: 250px; overflow-y: auto;">
              @for (notif of notifications; track notif.id) {
                <li [style.background]="notif.read ? 'white' : '#f0f8ff'"
                    style="padding: 10px; margin-bottom: 5px; border-radius: 4px; border: 1px solid #eee;">
                  <p style="margin: 0 0 5px 0; font-size: 14px;">{{ notif.message }}</p>
                  @if (!notif.read) {
                    <button type="button" (click)="markAsRead(notif.id)" style="font-size: 12px; padding: 4px 8px; border: none; background: #007bff; color: white; border-radius: 4px; cursor: pointer;">
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
  `
})
export class NotificationBellComponent implements OnInit {
  private http = inject(HttpClient);
  private apiUrl = '/api/v1/notifications';

  notifications: any[] = [];
  unreadCount = 0;
  isDropdownOpen = false;
  isLoading = false;
  hasError = false;

  ngOnInit(): void {
    this.loadNotifications();
  }

  loadNotifications(): void {
    this.isLoading = true;
    this.hasError = false;

    this.http.get<any[]>(this.apiUrl).subscribe({
      next: (data: any[]) => {
        this.notifications = Array.isArray(data) ? data : [];
        this.unreadCount = this.notifications.filter((n: any) => !n.read).length;
        this.isLoading = false;
      },
      error: () => {
        this.hasError = true;
        this.isLoading = false;
      }
    });
  }

  markAsRead(id: number): void {
    this.http.patch<any>(`${this.apiUrl}/${id}/read`, {}).subscribe({
      next: () => {
        const notif = this.notifications.find((n: any) => n.id === id);
        if (notif && !notif.read) {
          notif.read = true;
          this.unreadCount = Math.max(0, this.unreadCount - 1);
        }
      }
    });
  }

  toggleDropdown(): void {
    this.isDropdownOpen = !this.isDropdownOpen;
  }
}