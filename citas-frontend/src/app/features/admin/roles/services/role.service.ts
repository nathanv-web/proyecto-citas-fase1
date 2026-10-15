import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE_URL } from '../../../../core/config/api.config';

import { Role, RoleRequest, RoleUpdate, RolePermissionsRequest } from '../models/role.model';

/** El servicio consulta y administra roles, además del catálogo y la asignación de permisos. */
@Injectable({
  providedIn: 'root',
})
export class RoleService {
  private readonly http = inject(HttpClient);

  private readonly rolesUrl = `${API_BASE_URL}/roles`;

  private readonly permissionsUrl = `${API_BASE_URL}/permissions`;

  getAll(): Observable<Role[]> {
    return this.http.get<Role[]>(this.rolesUrl);
  }

  getById(id: number): Observable<Role> {
    return this.http.get<Role>(`${this.rolesUrl}/${id}`);
  }

  create(request: RoleRequest): Observable<Role> {
    return this.http.post<Role>(this.rolesUrl, request);
  }

  update(id: number, request: RoleUpdate): Observable<Role> {
    return this.http.put<Role>(`${this.rolesUrl}/${id}`, request);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.rolesUrl}/${id}`);
  }

  getPermissions(): Observable<string[]> {
    return this.http.get<string[]>(this.permissionsUrl);
  }

  assignPermissions(id: number, permisos: string[]): Observable<Role> {
    const request: RolePermissionsRequest = {
      permisos,
    };

    return this.http.put<Role>(`${this.rolesUrl}/${id}/permissions`, request);
  }
}
