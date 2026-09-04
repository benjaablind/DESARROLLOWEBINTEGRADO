export interface HistoriaClinica {
  id: number;
  pacienteNombre: string;
  odontologo: string;
  fecha: string;
  motivoConsulta: string;
  anamnesis: string;
  examenClinico: string;
  diagnostico: string;
  procedimiento: string;
  tratamiento: string;
  medicamentos: string;
  observaciones: string;
}

export type HistoriaClinicaRequest = Omit<HistoriaClinica, 'id'>;
