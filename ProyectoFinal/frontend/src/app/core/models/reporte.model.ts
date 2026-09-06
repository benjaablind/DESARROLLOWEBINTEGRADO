import { Archivo } from './archivo.model';
import { EstadoCita } from './cita.model';
import { EstadoPaciente } from './paciente.model';
import { EstadoTratamiento } from './tratamiento.model';

/**
 * Modelos de /api/reportes.
 * Los nombres coinciden uno a uno con los DTO del backend.
 * Las fechas llegan como cadenas ISO.
 */

/** Rango de fechas que aceptan los reportes. Ambos extremos son opcionales. */
export interface RangoFechas {
  desde?: string;
  hasta?: string;
}

export interface ReportePacienteItem {
  id: number;
  dni: string;
  nombreCompleto: string;
  edad: number | null;
  telefono: string;
  estado: EstadoPaciente;
  fechaRegistro: string;
}

export interface ReportePacientes {
  desde: string;
  hasta: string;
  total: number;
  activos: number;
  inactivos: number;
  pacientes: ReportePacienteItem[];
}

export interface ReporteCitaItem {
  id: number;
  pacienteNombre: string;
  odontologoNombre: string;
  fechaHora: string;
  motivo: string;
  estado: EstadoCita;
}

export interface ReporteCitas {
  desde: string;
  hasta: string;
  total: number;
  /** Conteo por estado. Incluye todos los estados, aunque queden en cero. */
  porEstado: Record<string, number>;
  citas: ReporteCitaItem[];
}

export interface ReporteTratamientoItem {
  id: number;
  pacienteNombre: string;
  nombre: string;
  estado: EstadoTratamiento;
  precio: number;
  totalPagado: number;
  saldoPendiente: number;
  fechaInicio: string | null;
  fechaFin: string | null;
}

export interface ReporteTratamientos {
  desde: string;
  hasta: string;
  totalRealizados: number;
  totalPendientes: number;
  montoRealizados: number;
  montoPendientes: number;
  tratamientos: ReporteTratamientoItem[];
}

export interface ReporteIngresos {
  desde: string;
  hasta: string;
  totalIngresos: number;
  cantidadPagos: number;
  /** Importe acumulado por metodo de pago. */
  porMetodo: Record<string, number>;
  /** Importe acumulado por mes, con la clave en formato "YYYY-MM". */
  porMes: Record<string, number>;
}

export interface ReportePagosPendientes {
  totalPendiente: number;
  cantidadTratamientos: number;
  items: ReporteTratamientoItem[];
}

export interface ReporteConsultaItem {
  id: number;
  fecha: string;
  motivoConsulta: string;
  diagnostico: string;
  anulada: boolean;
}

export interface ReporteHistorialPaciente {
  paciente: ReportePacienteItem;
  totalConsultas: number;
  totalCitas: number;
  totalTratamientos: number;
  totalArchivos: number;
  totalPagado: number;
  saldoPendiente: number;
  consultas: ReporteConsultaItem[];
  citas: ReporteCitaItem[];
  tratamientos: ReporteTratamientoItem[];
  archivos: Archivo[];
}

/** Fila generica para dibujar un grafico a partir de un mapa clave/valor. */
export interface FilaReporte {
  etiqueta: string;
  valor: number;
}

/** Convierte un mapa del backend en filas ordenadas, listas para graficar. */
export function aFilas(mapa: Record<string, number> | undefined): FilaReporte[] {
  if (!mapa) {
    return [];
  }
  return Object.entries(mapa).map(([etiqueta, valor]) => ({ etiqueta, valor }));
}
