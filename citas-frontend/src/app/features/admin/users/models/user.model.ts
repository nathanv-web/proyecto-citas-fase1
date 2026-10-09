export interface User {
  idUsuario: number;
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  activo: boolean;
  roles: string[];
}
