import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { User } from '../models/user.model';
import { UserRequest } from '../models/user-request.model';
import { UserUpdate } from '../models/user-update.model';
import { UserRolesRequest } from '../models/user-roles-request.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private readonly http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8081/api/v1/users';

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(this.apiUrl);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(
      `${this.apiUrl}/${id}`
    );
  }

  create(request: UserRequest): Observable<User> {
    return this.http.post<User>(
      this.apiUrl,
      request
    );
  }

  update(
    id: number,
    request: UserUpdate
  ): Observable<User> {
    return this.http.put<User>(
      `${this.apiUrl}/${id}`,
      request
    );
  }

  deactivate(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );
  }

  assignRoles(
    idUsuario: number,
    request: UserRolesRequest
  ): Observable<User> {
    return this.http.put<User>(
      `${this.apiUrl}/${idUsuario}/roles`,
      request
    );
  }
}
