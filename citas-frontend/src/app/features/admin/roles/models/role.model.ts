export interface Role {
  idRol: number;
  nombre: string;
  descripcion: string;
  permisos: string[];
}

export interface RoleRequest {
  nombre: string;
  descripcion: string;
}

export interface RoleUpdate {
  nombre?: string;
  descripcion?: string;
}

export interface RolePermissionsRequest {
  permisos: string[];
}
