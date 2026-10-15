/** El modelo define los datos enviados al registrar una cuenta de paciente. */
export interface RegisterRequest {
  nombre: string;
  apellido: string;
  correo: string;
  telefono: string;
  contrasena: string;
}
