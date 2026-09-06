import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import {
  RangoFechas,
  ReporteCitas,
  ReporteHistorialPaciente,
  ReporteIngresos,
  ReportePacientes,
  ReportePagosPendientes,
  ReporteTratamientos,
} from '../models/reporte.model';

/**
 * Consume /api/reportes.
 * Las fechas van en formato ISO "YYYY-MM-DD" y son opcionales: si no se envian,
 * el backend usa un rango amplio por defecto.
 */
@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/reportes';

  /** GET /api/reportes/pacientes */
  pacientes(rango: RangoFechas = {}): Observable<ReportePacientes> {
    return this.api.get<ReportePacientes>(`${this.ruta}/pacientes`, { ...rango });
  }

  /** GET /api/reportes/citas */
  citas(rango: RangoFechas = {}): Observable<ReporteCitas> {
    return this.api.get<ReporteCitas>(`${this.ruta}/citas`, { ...rango });
  }

  /** GET /api/reportes/tratamientos */
  tratamientos(rango: RangoFechas = {}): Observable<ReporteTratamientos> {
    return this.api.get<ReporteTratamientos>(`${this.ruta}/tratamientos`, { ...rango });
  }

  /** GET /api/reportes/ingresos */
  ingresos(rango: RangoFechas = {}): Observable<ReporteIngresos> {
    return this.api.get<ReporteIngresos>(`${this.ruta}/ingresos`, { ...rango });
  }

  /** GET /api/reportes/pagos-pendientes */
  pagosPendientes(): Observable<ReportePagosPendientes> {
    return this.api.get<ReportePagosPendientes>(`${this.ruta}/pagos-pendientes`);
  }

  /** GET /api/reportes/historial/{pacienteId} */
  historialDePaciente(pacienteId: number): Observable<ReporteHistorialPaciente> {
    return this.api.get<ReporteHistorialPaciente>(`${this.ruta}/historial/${pacienteId}`);
  }
}
