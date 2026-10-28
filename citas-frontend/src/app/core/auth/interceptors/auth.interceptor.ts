import { HttpErrorResponse, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, tap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';
import { API_BASE_URL } from '../../config/api.config';
import { NotificationService } from '../../notifications/notification.service';
import { apiErrorMessage } from '../../notifications/api-error';

/** Un único punto presenta los mensajes de la API para todas las pantallas. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (!req.url.startsWith(`${API_BASE_URL}/`)) return next(req);
  const auth = inject(AuthService);
  const router = inject(Router);
  const notifications = inject(NotificationService);
  const isPublic =
    req.url === `${API_BASE_URL}/auth/login` || req.url === `${API_BASE_URL}/auth/register`;
  const token = auth.getToken();
  const request =
    !isPublic && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(request).pipe(
    tap((event) => {
      if (event instanceof HttpResponse && req.method !== 'GET') {
        const message = (event.body as { message?: unknown } | null)?.message;
        if (typeof message === 'string') notifications.success(message);
      }
    }),
    catchError((error: HttpErrorResponse) => {
      notifications.error(apiErrorMessage(error));
      if (error.status === 401 && !isPublic) {
        auth.logout();
        void router.navigate(['/login']);
      }
      if (error.status === 403 && error.error?.code === 'PASSWORD_CHANGE_REQUIRED') {
        auth.setPasswordChangeRequired(true);
        void router.navigate(['/change-password']);
      }
      return throwError(() => error);
    }),
  );
};
