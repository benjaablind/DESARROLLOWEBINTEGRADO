export type EstadoCita =
  | 'PENDIENTE'
  | 'CONFIRMADA'
  | 'ATENDIDA'
  | 'CANCELADA'
  | 'NO_ASISTIO'
  | 'REPROGRAMADA';

export const ESTADOS_CITA: EstadoCita[] = [
  'PENDIENTE',
  'CONFIRMADA',
  'ATENDIDA',
  'CANCELADA',
  'NO_ASISTIO',
  'REPROGRAMADA',
];

export const ETIQUETAS_ESTADO_CITA: Record<EstadoCita, string> = {
  PENDIENTE: 'Pendiente',
  CONFIRMADA: 'Confirmada',
  ATENDIDA: 'Atendida',
  CANCELADA: 'Cancelada',
  NO_ASISTIO: 'No asistio',
  REPROGRAMADA: 'Reprogramada',
};

/** Clase de badge sugerida para pintar cada estado. */
export const BADGE_ESTADO_CITA: Record<EstadoCita, string> = {
  PENDIENTE: 'badge-advertencia',
  CONFIRMADA: 'badge-info',
  ATENDIDA: 'badge-exito',
  CANCELADA: 'badge-peligro',
  NO_ASISTIO: 'badge-peligro',
  REPROGRAMADA: 'badge-neutro',
};

export interface Cita {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  odontologoId: number;
  odontologoNombre: string;
  /** Fecha ISO con hora: "2026-09-04T10:00:00". */
  fechaHora: string;
  fechaHoraFin: string | null;
  duracionMinutos: number;
  motivo: string | null;
  estado: EstadoCita;
  observaciones: string | null;
  /** Presente solo si la cita nacio de una reprogramacion. */
  citaOriginalId: number | null;
  fechaRegistro: string;
}

export interface CitaRequest {
  pacienteId: number;
  odontologoId: number;
  fechaHora: string;
  /** Si se omite, el backend aplica 30 minutos. */
  duracionMinutos?: number | null;
  motivo?: string | null;
  observaciones?: string | null;
}

export interface CitaEstadoRequest {
  estado: EstadoCita;
  observaciones?: string | null;
}

export interface CitaReprogramarRequest {
  nuevaFechaHora: string;
  motivo?: string | null;
}

/** Filtros combinables del listado de citas. */
export interface FiltroCitas {
  /** Dia en formato ISO "2026-09-04". */
  dia?: string;
  pacienteId?: number;
  odontologoId?: number;
  estado?: EstadoCita;
}
