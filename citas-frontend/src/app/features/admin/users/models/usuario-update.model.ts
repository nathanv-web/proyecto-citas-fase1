// =================================================
// MODELO PARA ACTUALIZAR USUARIOS
// =================================================
// Los campos son opcionales para permitir actualizaciones parciales.

export interface UsuarioUpdate {

  nombre?: string;

  apellido?: string;

  correo?: string;

  telefono?: string;

  activo?: boolean;

}
