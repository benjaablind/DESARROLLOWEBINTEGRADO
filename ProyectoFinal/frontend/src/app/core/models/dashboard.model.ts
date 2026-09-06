import { Cita } from './cita.model';
import { EstadoTratamiento } from './tratamiento.model';

export interface DashboardPacienteResumen {
  id: number;
  nombreCompleto: string;
  telefono: string | null;
  /** Fecha ISO con hora. */
  fechaRegistro: string;
}

export interface DashboardTratamientoResumen {
  id: number;
  pacienteId: number;
  pacienteNombre: string;
  nombre: string;
  estado: EstadoTratamiento;
  precio: number;
  saldoPendiente: number;
}

export interface DashboardResumenActividad {
  citasAtendidasMes: number;
  citasCanceladasMes: number;
  citasNoAsistioMes: number;
  tratamientosCompletadosMes: number;
  nuevosPacientesMes: number;
}

/** Indicadores de la pantalla principal. */
export interface Dashboard {
  pacientesRegistrados: number;
  citasHoy: number;
  citasPendientes: number;
  tratamientosActivos: number;
  pagosPendientes: number;
  ingresosDelMes: number;
  proximasCitas: Cita[];
  pacientesRecientes: DashboardPacienteResumen[];
  tratamientosActivosDetalle: DashboardTratamientoResumen[];
  alertas: string[];
  resumenActividad: DashboardResumenActividad;
}
