/** El modelo contiene los datos que pueden actualizarse en un usuario existente. */
export interface UserUpdate {
  nombre?: string;
  apellido?: string;
  correo?: string;
  telefono?: string;
  activo?: boolean;
}
