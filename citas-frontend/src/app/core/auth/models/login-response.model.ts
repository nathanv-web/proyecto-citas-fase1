export interface LoginUsuario {
  idUsuario: number;
  nombre: string;
  apellido: string;
  correo: string;
  roles: string[];
  permisos: string[];
}

export interface LoginResponse {
  token: string;
  tipo: string;
  usuario: LoginUsuario;
}
