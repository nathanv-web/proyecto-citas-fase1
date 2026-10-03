export interface Appointment {
  idCita: number;
  paciente: string;
  medico: string;
  especialidad: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: string;
  motivo: string;
  diagnosticoReceta?: string;
  observaciones?: string;
}
