import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, from, map, switchMap } from 'rxjs';
import { Preferences } from '@capacitor/preferences';
import { API_BASE_URL } from '../../config/api.config';

import { LoginRequest } from '../models/login-request';
import { LoginResponse, LoginUsuario } from '../models/login-response';
import { RegisterRequest } from '../models/register-request';
import { RegisterResponse } from '../models/register-response';

@Injectable({ providedIn: 'root' })
export class Auth {
  private readonly apiUrl = `${API_BASE_URL}/auth`;
  private readonly tokenKey = 'auth_token';
  private readonly userKey = 'auth_user';
  private token: string | null = null;
  private usuario: LoginUsuario | null = null;
  private sessionRestored = false;
  private restorePromise?: Promise<boolean>;

  constructor(private http: HttpClient) {}

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, credentials).pipe(
      switchMap((response) => from(this.storeSession(response)).pipe(map(() => response))),
    );
  }

  register(data: RegisterRequest): Observable<RegisterResponse> {
    return this.http.post<RegisterResponse>(`${this.apiUrl}/register`, data);
  }

  async restoreSession(): Promise<boolean> {
    if (this.sessionRestored) {
      return this.isAuthenticated();
    }
    if (!this.restorePromise) {
      this.restorePromise = this.readStoredSession().finally(() => {
        this.sessionRestored = true;
        this.restorePromise = undefined;
      });
    }
    return this.restorePromise;
  }

  logout(): void {
    this.token = null;
    this.usuario = null;
    this.sessionRestored = true;
    this.removeLegacySession();
    void Promise.all([
      Preferences.remove({ key: this.tokenKey }),
      Preferences.remove({ key: this.userKey }),
    ]).catch(() => undefined);
  }

  getToken(): string | null {
    return this.token;
  }

  getUsuario(): LoginUsuario | null {
    return this.usuario;
  }

  isAuthenticated(): boolean {
    return this.token !== null;
  }

  hasRole(role: string): boolean {
    return this.usuario?.roles?.includes(role) ?? false;
  }

  hasPermission(permission: string): boolean {
    return this.usuario?.permisos?.includes(permission) ?? false;
  }

  private async readStoredSession(): Promise<boolean> {
    try {
      const [storedToken, storedUser] = await Promise.all([
        Preferences.get({ key: this.tokenKey }),
        Preferences.get({ key: this.userKey }),
      ]);
      let token = storedToken.value;
      let userJson = storedUser.value;
      const oldToken = this.readLegacyValue(this.tokenKey);
      const oldUser = this.readLegacyValue(this.userKey);

      if (!token && oldToken) {
        await Preferences.set({ key: this.tokenKey, value: oldToken });
        token = oldToken;
      }
      if (!userJson && oldUser) {
        await Preferences.set({ key: this.userKey, value: oldUser });
        userJson = oldUser;
      }
      this.removeLegacySession();

      if (!token || !userJson) {
        await this.removeStoredSession();
        return false;
      }

      const user = JSON.parse(userJson) as LoginUsuario;
      if (!user || typeof user.idUsuario !== 'number') {
        await this.removeStoredSession();
        return false;
      }
      this.token = token;
      this.usuario = user;
      return true;
    } catch {
      this.token = null;
      this.usuario = null;
      this.removeLegacySession();
      return false;
    }
  }

  private async storeSession(response: LoginResponse): Promise<void> {
    await Promise.all([
      Preferences.set({ key: this.tokenKey, value: response.token }),
      Preferences.set({ key: this.userKey, value: JSON.stringify(response.usuario) }),
    ]);
    this.token = response.token;
    this.usuario = response.usuario;
    this.sessionRestored = true;
  }

  private async removeStoredSession(): Promise<void> {
    this.token = null;
    this.usuario = null;
    await Promise.all([
      Preferences.remove({ key: this.tokenKey }),
      Preferences.remove({ key: this.userKey }),
    ]);
  }

  private readLegacyValue(key: string): string | null {
    return typeof localStorage === 'undefined' ? null : localStorage.getItem(key);
  }

  private removeLegacySession(): void {
    if (typeof localStorage !== 'undefined') {
      localStorage.removeItem(this.tokenKey);
      localStorage.removeItem(this.userKey);
    }
  }
}
