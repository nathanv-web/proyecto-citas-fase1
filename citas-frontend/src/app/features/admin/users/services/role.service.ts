import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Role } from '../models/role.model';

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8081/api/v1/roles';

  listarRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(this.apiUrl);
  }
}