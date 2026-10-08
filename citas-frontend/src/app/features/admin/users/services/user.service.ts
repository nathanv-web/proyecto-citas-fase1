import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// =================================================
// MODELOS
// =================================================

import { User } from '../models/user.model';
import { UserRequest } from '../models/user-request.model';
import { UserUpdate } from '../models/user-update.model';
import { UserRolesRequest } from '../models/user-roles-request.model';


@Injectable({
  providedIn: 'root'
})
export class UserService {

  // =================================================
  // DEPENDENCIAS
  // =================================================

  private http = inject(HttpClient);

  // =================================================
  // URL DEL BACKEND
  // =================================================

  private readonly apiUrl =
    'http://localhost:8081/api/v1/users';


  // =================================================
  // LISTAR USUARIOS
  // =================================================
  // Permiso requerido: VER_USUARIOS

  listarUsuarios(): Observable<User[]> {

    return this.http.get<User[]>(
      this.apiUrl
    );

  }


  // =================================================
  // BUSCAR USUARIO POR ID
  // =================================================
  // Permiso requerido: VER_USUARIOS

  buscarUsuario(id: number): Observable<User> {

    return this.http.get<User>(
      `${this.apiUrl}/${id}`
    );

  }


  // =================================================
  // CREAR USUARIO
  // =================================================
  // Permiso requerido: CREAR_USUARIOS

  crearUsuario(
    usuario: UserRequest
  ): Observable<User> {

    return this.http.post<User>(
      this.apiUrl,
      usuario
    );

  }


  // =================================================
  // ACTUALIZAR USUARIO
  // =================================================
  // Permiso requerido: ACTUALIZAR_USUARIOS

  actualizarUsuario(
    id: number,
    usuario: UserUpdate
  ): Observable<User> {

    return this.http.put<User>(
      `${this.apiUrl}/${id}`,
      usuario
    );

  }


  // =================================================
  // DESACTIVAR USUARIO
  // =================================================
  // Permiso requerido: DESACTIVAR_USUARIOS
  // El backend responde 204 No Content.

  desactivarUsuario(
    id: number
  ): Observable<void> {

    return this.http.delete<void>(
      `${this.apiUrl}/${id}`
    );

  }


  // =================================================
  // ASIGNAR ROL A USUARIO
  // =================================================
  // Permiso requerido: ASIGNAR_ROLES

  asignarRol(
    idUsuario: number,
    request: UserRolesRequest
  ): Observable<User> {

    return this.http.put<User>(
      `${this.apiUrl}/${idUsuario}/roles`,
      request
    );

  }

}