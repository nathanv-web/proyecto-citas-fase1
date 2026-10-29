import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from '../models/user.model';
import { UserRequest } from '../models/user-request.model';
import { UserUpdate } from '../models/user-update.model';
import { UserRolesRequest } from '../models/user-roles-request.model';
import { API_BASE_URL } from '../../../../core/config/api.config';

export interface UserFilters {
  search?: string;
  activo?: boolean;
}

/** El servicio reúne las solicitudes de usuarios, sus cambios de estado y la asignación de roles. */
@Injectable({
  providedIn: 'root',
})
export class UserService {
  private readonly http = inject(HttpClient);

  private readonly apiUrl = `${API_BASE_URL}/users`;

  getAll(filters: UserFilters = {}): Observable<User[]> {
    let params = new HttpParams();
    if (filters.search?.trim()) params = params.set('search', filters.search.trim());
    if (filters.activo !== undefined) params = params.set('activo', filters.activo);
    return this.http.get<User[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  create(request: UserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, request);
  }

  update(id: number, request: UserUpdate): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, request);
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  activate(id: number): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, { activo: true });
  }

  assignRoles(idUsuario: number, request: UserRolesRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${idUsuario}/roles`, request);
  }
}
