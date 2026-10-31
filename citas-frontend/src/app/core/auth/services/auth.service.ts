import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize, shareReplay, tap } from 'rxjs';

import { LoginRequest } from '../models/login-request.model';
import { LoginResponse, LoginUsuario } from '../models/login-response.model';
import { API_BASE_URL } from '../../config/api.config';
import { RegisterRequest } from '../models/register-request.model';

/** El servicio gestiona la autenticaciÃ³n, conserva la sesiÃ³n local y consulta los roles y permisos recibidos. */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private sessionRequest?: Observable<Pick<LoginResponse, 'usuario' | 'debeCambiarPassword'>>;
  private readonly apiUrl = `${API_BASE_URL}/auth`;
  private readonly tokenKey = 'auth_token';
  private readonly userKey = 'auth_user';

  constructor(private http: HttpClient) {}

  register(request: RegisterRequest): Observable<unknown> {
    return this.http.post(`${this.apiUrl}/register`, request);
  }

  // La sesiÃ³n se guarda Ãºnicamente despuÃ©s de una respuesta correcta de la API.
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        this.setPasswordChangeRequired(response.debeCambiarPassword);
        localStorage.setItem(this.tokenKey, response.token);
        localStorage.setItem(this.userKey, JSON.stringify(response.usuario));
      }),
    );
  }

  setPasswordChangeRequired(required: boolean): void {
    localStorage.setItem('auth_password_change', String(required));
  }

  mustChangePassword(): boolean {
    return localStorage.getItem('auth_password_change') === 'true';
  }

  refreshSession(): Observable<Pick<LoginResponse, 'usuario' | 'debeCambiarPassword'>> {
    if (this.sessionRequest) return this.sessionRequest;
    this.sessionRequest = this.http
      .get<Pick<LoginResponse, 'usuario' | 'debeCambiarPassword'>>(`${API_BASE_URL}/me`)
      .pipe(
        tap((response) => {
          localStorage.setItem(this.userKey, JSON.stringify(response.usuario));
          this.setPasswordChangeRequired(response.debeCambiarPassword);
        }),
        finalize(() => {
          this.sessionRequest = undefined;
        }),
        shareReplay({ bufferSize: 1, refCount: true }),
      );
    return this.sessionRequest;
  }

  changePassword(actual: string, nueva: string): Observable<{ message: string }> {
    return this.http.put<{ message: string }>(`${API_BASE_URL}/me/password`, { actual, nueva });
  }

  forgotPassword(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/password/forgot`,
      { email }
    );
  }

  resetPassword(token: string, nueva: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/password/reset`,
      { token, nueva }
    ).pipe(tap(() => this.logout()));
  }

  requestEmailVerification(email: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/email/request`,
      { email }
    );
  }

  verifyEmail(token: string): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/email/verify`,
      { token }
    );
  }
  logout(): void {
    localStorage.removeItem('auth_password_change');
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  // Una sesiÃ³n con datos ilegibles se descarta para evitar permisos inconsistentes.
  getUsuario(): LoginUsuario | null {
    const usuario = localStorage.getItem(this.userKey);

    if (!usuario) {
      return null;
    }

    try {
      return JSON.parse(usuario) as LoginUsuario;
    } catch {
      this.logout();
      return null;
    }
  }

  hasRole(role: string): boolean {
    const usuario = this.getUsuario();

    return usuario?.roles?.includes(role) ?? false;
  }

  hasPermission(permission: string): boolean {
    const usuario = this.getUsuario();

    return usuario?.permisos?.includes(permission) ?? false;
  }
}
