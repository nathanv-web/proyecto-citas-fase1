import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Role, RoleRequest, Permission } from '../models/role';

@Injectable({
  providedIn: 'root'
})
export class RolService {
  private http = inject(HttpClient);
  private apiUrl = 'http://localhost:8080/api/v1';

  // 1. Obtener todos los roles
  getRoles(): Observable<Role[]> {
    return this.http.get<Role[]>(`${this.apiUrl}/roles`);
  }

  // 2. Obtener un rol por su ID
  getRoleById(id: number): Observable<Role> {
    return this.http.get<Role>(`${this.apiUrl}/roles/${id}`);
  }

  // 3. Crear un nuevo rol
  createRole(role: RoleRequest): Observable<Role> {
    return this.http.post<Role>(`${this.apiUrl}/roles`, role);
  }

  // 4. Editar/Actualizar un rol
  updateRole(id: number, role: RoleRequest): Observable<Role> {
    return this.http.put<Role>(`${this.apiUrl}/roles/${id}`, role);
  }

  // 5. Eliminar un rol
  deleteRole(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/roles/${id}`);
  }

  // 6. Obtener la lista dinámica de todos los permisos disponibles
  getPermissions(): Observable<Permission[]> {
    return this.http.get<Permission[]>(`${this.apiUrl}/permissions`);
  }

  // 7. Guardar/Reemplazar los permisos asignados a un rol
  updateRolePermissions(id: number, permissionIds: number[]): Observable<Role> {
    return this.http.put<Role>(`${this.apiUrl}/roles/${id}/permissions`, permissionIds);
  }
}