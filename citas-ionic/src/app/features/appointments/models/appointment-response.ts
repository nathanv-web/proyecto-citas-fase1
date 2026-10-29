export interface AppointmentResponse {
  idCita: number;
  idMedico: number;
  paciente: string;
  medico: string;
  especialidad: string;
  fecha: string;
  horaInicio: string;
  horaFin: string;
  estado: string;
  motivo: string;
  motivoCancelacion: string | null;
  diagnosticoReceta: string | null;
  observaciones: string | null;
}