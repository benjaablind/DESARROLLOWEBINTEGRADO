export type EstadoTratamiento =
  | 'PENDIENTE'
  | 'APROBADO'
  | 'EN_PROCESO'
  | 'COMPLETADO'
  | 'CANCELADO';

export const ESTADOS_TRATAMIENTO: EstadoTratamiento[] = [
  'PENDIENTE',
  'APROBADO',
  'EN_PROCESO',
  'COMPLETADO',
  'CANCELADO',
];

export const ETIQUETAS_ESTADO_TRATAMIENTO: Record<EstadoTratamiento, string> = {
  PENDIENTE: 'Pendiente',
  APROBADO: 'Aprobado',
  EN_PROCESO: 'En proceso',
  COMPLETADO: 'Completado',
  CANCELADO: 'Cancelado',
};

export const BADGE_ESTADO_TRATAMIENTO: Record<EstadoTratamiento, string> = {
  PENDIENTE: 'badge-advertencia',
  APROBADO: 'badge-info',
  EN_PROCESO: 'badge-primario',
  COMPLETADO: 'badge-exito',
  CANCELADO: 'badge-peligro',
};

/** Sesion planificada dentro de un tratamiento. */
export interface SesionTratamiento {
  id: number;
  numero: number;
  /** Fecha ISO (solo dia). */
  fecha: string | null;
  descripcion: string | null;
  realizada: boolean;
  observaciones: string | null;
}

export interface Tratamiento {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  odontologoId: number;
  odontologoNombre: string;
  nombre: string;
  descripcion: string | null;
  precio: number;
  estado: EstadoTratamiento;
  /** Fechas ISO (solo dia). */
  fechaInicio: string | null;
  fechaFin: string | null;
  observaciones: string | null;
  sesiones: SesionTratamiento[];
  totalPagado: number;
  saldoPendiente: number;
  fechaRegistro: string;
}

export interface TratamientoRequest {
  pacienteId: number;
  odontologoId: number;
  nombre: string;
  descripcion?: string | null;
  precio: number;
  fechaInicio?: string | null;
  observaciones?: string | null;
}

export interface TratamientoEstadoRequest {
  estado: EstadoTratamiento;
}

export interface SesionRequest {
  fecha: string | null;
  descripcion: string;
  observaciones?: string | null;
}

export interface SesionEstadoRequest {
  realizada: boolean;
  observaciones?: string | null;
}

/** Filtros opcionales del listado de tratamientos. */
export interface FiltroTratamientos {
  pacienteId?: number;
  odontologoId?: number;
  estado?: EstadoTratamiento;
}
