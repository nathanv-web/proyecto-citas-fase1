import { NotificationService } from '../../notifications/notification.service';
import { apiErrorMessage } from '../../notifications/api-error';
import { Injectable } from '@angular/core';
import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandler,
  HttpInterceptor,
  HttpRequest
} from '@angular/common/http';

import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  private readonly tokenKey = 'auth_token';
  private readonly userKey = 'auth_user';

  constructor(private router: Router, private notifications: NotificationService) {}

  intercept(
    request: HttpRequest<unknown>,
    next: HttpHandler
  ): Observable<HttpEvent<unknown>> {

    if (!request.url.startsWith('http://localhost:8081/api/v1/')) return next.handle(request);

    const isPublic = /\/auth\/(login|register)$/.test(request.url);
    const token = localStorage.getItem(this.tokenKey);

    let authRequest = request;

    if (token && !isPublic) {
      authRequest = request.clone({
        setHeaders: {
          Authorization: `Bearer ${token}`
        }
      });
    }

    return next.handle(authRequest).pipe(
      catchError((error: HttpErrorResponse) => {

        void this.notifications.error(apiErrorMessage(error));
        if (error.status === 401 && !isPublic) {
          localStorage.removeItem(this.tokenKey);
          localStorage.removeItem(this.userKey);

          this.router.navigateByUrl('/login', {
            replaceUrl: true
          });
        }

        return throwError(() => error);
      })
    );
  }
}
