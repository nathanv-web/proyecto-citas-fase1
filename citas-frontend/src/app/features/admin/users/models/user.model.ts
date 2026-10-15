/** El modelo describe un usuario, sus datos de contacto, su estado y sus roles. */
export interface User {
  idUsuario: number;
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  activo: boolean;
  roles: string[];
}
