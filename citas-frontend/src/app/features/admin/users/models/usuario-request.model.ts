// =================================================
// MODELO PARA CREAR USUARIOS
// =================================================
// Los nombres deben coincidir con UsuarioRequestDTO del backend.

export interface UsuarioRequest {

  nombre: string;

  apellido: string;

  correo: string;

  telefono: string;

  contrasena: string;

}