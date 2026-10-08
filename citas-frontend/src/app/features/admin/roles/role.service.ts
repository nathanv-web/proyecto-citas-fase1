import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';

export interface Role {
  idRol?: number;
  nombre: string;
  descripcion?: string;
  permisos?: string[];
}

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private http = inject(HttpClient);

  private apiUrl =
    'http://localhost:8081/api/v1/roles';

  private permissionsUrl =
    'http://localhost:8081/api/v1/permissions';


  // ===============================
  // LISTAR ROLES
  // ===============================

  getRoles() {

    return this.http.get<Role[]>(
      this.apiUrl
    );
  }


  // ===============================
  // BUSCAR ROL
  // ===============================

  getRole(id: number) {

    return this.http.get<Role>(
      `${this.apiUrl}/${id}`
    );
  }


  // ===============================
  // CREAR
  // ===============================

  createRole(role: Role) {

    return this.http.post<Role>(
      this.apiUrl,
      role
    );
  }


  // ===============================
  // ACTUALIZAR
  // ===============================

  updateRole(
    id: number,
    role: Role
  ) {

    return this.http.put<Role>(
      `${this.apiUrl}/${id}`,
      role
    );
  }


  // ===============================
  // ELIMINAR
  // ===============================

  deleteRole(id: number) {

    return this.http.delete(
      `${this.apiUrl}/${id}`
    );
  }


  // ===============================
  // LISTAR PERMISOS
  // ===============================

  getPermissions() {

    return this.http.get<string[]>(
      this.permissionsUrl
    );
  }


  // ===============================
  // ASIGNAR PERMISOS
  // ===============================

  assignPermissions(
    id: number,
    permissions: string[]
  ) {

    const body = {

      permisos: permissions

    };

    return this.http.put<Role>(
      `${this.apiUrl}/${id}/permissions`,
      body
    );
  }
}