/** El modelo describe un rol y los permisos que tiene asignados. */
export interface Role {
  idRol: number;
  nombre: string;
  descripcion: string;
  permisos: string[];
}

/** El modelo reúne el nombre y la descripción necesarios para crear un rol. */
export interface RoleRequest {
  nombre: string;
  descripcion: string;
}

/** El modelo contiene los datos que pueden actualizarse en un rol. */
export interface RoleUpdate {
  nombre?: string;
  descripcion?: string;
}

/** La solicitud contiene los nombres de los permisos asignados al rol. */
export interface RolePermissionsRequest {
  permisos: string[];
}
