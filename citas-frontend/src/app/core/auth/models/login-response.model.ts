/** El modelo describe la identidad, los roles y los permisos recibidos durante el inicio de sesión. */
export interface LoginUsuario {
  idUsuario: number;
  nombre: string;
  apellido: string;
  correo: string;
  telefono?: string | null;
  roles: string[];
  permisos: string[];
}

/** La respuesta contiene el token, su tipo y los datos de la cuenta autenticada. */
export interface LoginResponse {
  debeCambiarPassword: boolean;
  token: string;
  tipo: string;
  usuario: LoginUsuario;
}
