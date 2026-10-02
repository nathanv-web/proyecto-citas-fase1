import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// =================================================
// MODELOS
// =================================================

import { Usuario } from '../models/usuario.model';
import { UsuarioRequest } from '../models/usuario-request.model';
import { UsuarioUpdate } from '../models/usuario-update.model';
import { UsuarioRolesRequest } from '../models/usuario-roles-request.model';


@Injectable({
  providedIn: 'root'
})
export class UsuarioService {

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

  listarUsuarios(): Observable<Usuario[]> {

    return this.http.get<Usuario[]>(
      this.apiUrl
    );

  }


  // =================================================
  // BUSCAR USUARIO POR ID
  // =================================================
  // Permiso requerido: VER_USUARIOS

  buscarUsuario(id: number): Observable<Usuario> {

    return this.http.get<Usuario>(
      `${this.apiUrl}/${id}`
    );

  }


  // =================================================
  // CREAR USUARIO
  // =================================================
  // Permiso requerido: CREAR_USUARIOS

  crearUsuario(
    usuario: UsuarioRequest
  ): Observable<Usuario> {

    return this.http.post<Usuario>(
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
    usuario: UsuarioUpdate
  ): Observable<Usuario> {

    return this.http.put<Usuario>(
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
    request: UsuarioRolesRequest
  ): Observable<Usuario> {

    return this.http.put<Usuario>(
      `${this.apiUrl}/${idUsuario}/roles`,
      request
    );

  }

}