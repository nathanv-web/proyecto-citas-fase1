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

export interface DoctorRequest {
  idUsuario: number;
  idEspecialidad: number;
  colegiado: string;
  aniosExperiencia: number;
  biografia: string;
}
