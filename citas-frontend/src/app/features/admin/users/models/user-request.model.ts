/** El modelo reúne los datos necesarios para crear un usuario. */
export interface UserRequest {
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  contrasena: string;
}
