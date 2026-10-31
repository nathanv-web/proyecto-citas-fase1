import { NotificationService } from '../../notifications/notification.service';
import { apiErrorMessage } from '../../notifications/api-error';
import { API_BASE_URL } from '../../config/api.config';
import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';

import { Router } from '@angular/router';
import { Observable, catchError, from, switchMap, throwError } from 'rxjs';
import { Auth } from '../services/auth';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(
    private router: Router,
    private notifications: NotificationService,
    private auth: Auth,
  ) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {

    if (!request.url.startsWith(`${API_BASE_URL}/`)) {
      return next.handle(request);
    }

    const isPublic = /\/auth\/(login|register)(?:[?#]|$)/.test(request.url);
    const sendRequest = (): Observable<HttpEvent<unknown>> => {
      const token = this.auth.getToken();
      const authRequest = token && !isPublic
        ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
        : request;
      return next.handle(authRequest);
    };

    const response = isPublic
      ? sendRequest()
      : from(this.auth.restoreSession()).pipe(switchMap(() => sendRequest()));

    return response.pipe(
      catchError((error: HttpErrorResponse) => {
        void this.notifications.error(apiErrorMessage(error));
        if (error.status === 401 && !isPublic) {
          this.auth.logout();
          this.router.navigateByUrl('/login', {
            replaceUrl: true
          });
        }

        return throwError(() => error);
      })
    );
  }
}
