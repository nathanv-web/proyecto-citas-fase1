import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';

import { LoginRequest } from '../models/login-request.model';
import { LoginResponse, LoginUsuario } from '../models/login-response.model';
import { API_BASE_URL } from '../../config/api.config';
import { RegisterRequest } from '../models/register-request.model';

/** El servicio gestiona la autenticación, conserva la sesión local y consulta los roles y permisos recibidos. */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = `${API_BASE_URL}/auth`;
  private readonly tokenKey = 'auth_token';
  private readonly userKey = 'auth_user';

  constructor(private http: HttpClient) {}

  register(request: RegisterRequest): Observable<unknown> {
    return this.http.post(`${this.apiUrl}/register`, request);
  }

  // La sesión se guarda únicamente después de una respuesta correcta de la API.
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      tap((response) => {
        localStorage.setItem(this.tokenKey, response.token);
        localStorage.setItem(this.userKey, JSON.stringify(response.usuario));
      }),
    );
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    localStorage.removeItem(this.userKey);
  }

  isAuthenticated(): boolean {
    return this.getToken() !== null;
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  // Una sesión con datos ilegibles se descarta para evitar permisos inconsistentes.
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
