/** El modelo describe el perfil profesional y el estado de un médico. */
export interface Doctor {
  idMedico: number;
  nombre: string;
  correo: string;
  especialidad: string;
  colegiado: string;
  aniosExperiencia: number;
  biografia: string;
  estado: string;
}

/** La solicitud relaciona un usuario y una especialidad con los datos del perfil médico. */
export interface DoctorRequest {
  idUsuario: number;
  idEspecialidad: number;
  colegiado: string;
  aniosExperiencia: number;
  biografia: string;
}

/** Datos que un administrador puede modificar en un perfil médico existente. */
export interface DoctorUpdateRequest {
  idEspecialidad: number;
  colegiado: string;
  aniosExperiencia: number;
  biografia: string;
}
