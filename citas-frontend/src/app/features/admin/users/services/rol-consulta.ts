import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Rol } from '../models/rol.model';

@Injectable({
  providedIn: 'root'
})
export class RolConsultaService {

  private http = inject(HttpClient);

  private readonly apiUrl =
    'http://localhost:8081/api/v1/roles';

  listarRoles(): Observable<Rol[]> {
    return this.http.get<Rol[]>(this.apiUrl);
  }
}