/** Registro de una atencion clinica del paciente. */
export interface HistoriaClinica {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  odontologoId: number;
  odontologoNombre: string;
  /** Cita de la que proviene la consulta. Es opcional. */
  citaId: number | null;
  /** Fecha ISO con hora. */
  fecha: string;
  motivoConsulta: string | null;
  anamnesis: string | null;
  examenClinico: string | null;
  diagnostico: string | null;
  procedimiento: string | null;
  tratamiento: string | null;
  medicamentos: string | null;
  observaciones: string | null;
  anulada: boolean;
  motivoAnulacion: string | null;
}

export interface HistoriaClinicaRequest {
  pacienteId: number;
  odontologoId: number;
  citaId?: number | null;
  motivoConsulta: string;
  anamnesis?: string | null;
  examenClinico?: string | null;
  diagnostico?: string | null;
  procedimiento?: string | null;
  tratamiento?: string | null;
  medicamentos?: string | null;
  observaciones?: string | null;
}

export interface HistoriaAnularRequest {
  motivo: string;
}
