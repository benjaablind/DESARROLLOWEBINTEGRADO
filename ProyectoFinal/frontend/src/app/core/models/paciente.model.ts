export type EstadoPaciente = 'ACTIVO' | 'INACTIVO';

export const ESTADOS_PACIENTE: EstadoPaciente[] = ['ACTIVO', 'INACTIVO'];

export const ETIQUETAS_ESTADO_PACIENTE: Record<EstadoPaciente, string> = {
  ACTIVO: 'Activo',
  INACTIVO: 'Inactivo',
};

/** Antecedentes medicos y odontologicos del paciente. */
export interface Antecedentes {
  enfermedades: string[];
  alergias: string[];
  medicamentos: string[];
  habitos: string[];
  antecedentesOdontologicos: string | null;
  observaciones: string | null;
}

/** Ficha completa del paciente. */
export interface Paciente {
  id: number;
  dni: string;
  nombres: string;
  apellidos: string;
  nombreCompleto: string;
  edad: number | null;
  /** Fecha ISO (solo dia): "1990-05-12". */
  fechaNacimiento: string | null;
  sexo: string | null;
  telefono: string | null;
  email: string | null;
  direccion: string | null;
  distrito: string | null;
  ciudad: string | null;
  estado: EstadoPaciente;
  antecedentes: Antecedentes;
  /** Fecha ISO con hora. */
  fechaRegistro: string;
}

/** Fila del listado de pacientes. */
export interface PacienteResumen {
  id: number;
  dni: string;
  nombreCompleto: string;
  edad: number | null;
  telefono: string | null;
  estado: EstadoPaciente;
}

export interface AntecedentesRequest {
  enfermedades?: string[];
  alergias?: string[];
  medicamentos?: string[];
  habitos?: string[];
  antecedentesOdontologicos?: string | null;
  observaciones?: string | null;
}

export interface PacienteRequest {
  dni: string;
  nombres: string;
  apellidos: string;
  fechaNacimiento: string | null;
  sexo?: string | null;
  telefono?: string | null;
  email?: string | null;
  direccion?: string | null;
  distrito?: string | null;
  ciudad?: string | null;
  antecedentes?: AntecedentesRequest | null;
}

/** Filtros opcionales del listado de pacientes. */
export interface FiltroPacientes {
  texto?: string;
  estado?: EstadoPaciente;
}
