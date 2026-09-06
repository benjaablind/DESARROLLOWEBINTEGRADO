import { Tratamiento } from './tratamiento.model';

export type MetodoPago = 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA' | 'YAPE' | 'PLIN';

export const METODOS_PAGO: MetodoPago[] = [
  'EFECTIVO',
  'TARJETA',
  'TRANSFERENCIA',
  'YAPE',
  'PLIN',
];

export const ETIQUETAS_METODO_PAGO: Record<MetodoPago, string> = {
  EFECTIVO: 'Efectivo',
  TARJETA: 'Tarjeta',
  TRANSFERENCIA: 'Transferencia',
  YAPE: 'Yape',
  PLIN: 'Plin',
};

export interface Pago {
  id: number;
  tratamientoId: number;
  tratamientoNombre: string;
  pacienteId: number;
  pacienteNombre: string;
  monto: number;
  metodo: MetodoPago;
  /** Fecha ISO con hora. */
  fecha: string;
  comprobante: string | null;
  observaciones: string | null;
}

export interface PagoRequest {
  tratamientoId: number;
  monto: number;
  metodo: MetodoPago;
  comprobante?: string | null;
  observaciones?: string | null;
}

/** Resumen economico del paciente. */
export interface EstadoCuenta {
  pacienteId: number;
  pacienteNombre: string;
  totalTratamientos: number;
  totalPagado: number;
  saldoPendiente: number;
  tratamientos: Tratamiento[];
}

/** Filtros opcionales del listado de pagos. */
export interface FiltroPagos {
  pacienteId?: number;
  tratamientoId?: number;
  metodo?: MetodoPago;
  /** Fechas ISO (solo dia). */
  desde?: string;
  hasta?: string;
}
