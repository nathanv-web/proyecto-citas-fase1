/** El modelo describe una especialidad médica y su estado de actividad. */
export interface Specialty {
  idEspecialidad?: number;
  nombre: string;
  descripcion: string;
  activo?: boolean;
}
