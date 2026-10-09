import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import {
  Role,
  RoleRequest,
  RoleUpdate,
  RolePermissionsRequest
} from '../models/role.model';

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private readonly http = inject(HttpClient);

  private readonly rolesUrl =
    'http://localhost:8081/api/v1/roles';

  private readonly permissionsUrl =
    'http://localhost:8081/api/v1/permissions';

  getAll(): Observable<Role[]> {
    return this.http.get<Role[]>(this.rolesUrl);
  }

  getById(id: number): Observable<Role> {
    return this.http.get<Role>(
      `${this.rolesUrl}/${id}`
    );
  }

  create(request: RoleRequest): Observable<Role> {
    return this.http.post<Role>(
      this.rolesUrl,
      request
    );
  }

  update(
    id: number,
    request: RoleUpdate
  ): Observable<Role> {
    return this.http.put<Role>(
      `${this.rolesUrl}/${id}`,
      request
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.rolesUrl}/${id}`
    );
  }

  getPermissions(): Observable<string[]> {
    return this.http.get<string[]>(
      this.permissionsUrl
    );
  }

  assignPermissions(
    id: number,
    permisos: string[]
  ): Observable<Role> {

    const request: RolePermissionsRequest = {
      permisos
    };

    return this.http.put<Role>(
      `${this.rolesUrl}/${id}/permissions`,
      request
    );
  }
}
