// =================================================
// MODELO DE USUARIO
// =================================================
// Representa los datos que devuelve el backend.
// Nunca contiene la contraseña del usuario.

export interface Usuario {

  idUsuario: number;

  nombre: string;

  apellido: string;

  correo: string;

  telefono: string;

  activo: boolean;

  roles: string[];

}