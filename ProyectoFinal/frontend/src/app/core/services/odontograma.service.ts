import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import {
  Odontograma,
  OdontogramaHistorial,
  OdontogramaPieza,
  OdontogramaRegistroRequest,
  ResumenOdontograma,
} from '../models/odontograma.model';

/** Consume /api/odontograma. */
@Injectable({ providedIn: 'root' })
export class OdontogramaService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/odontograma';

  /** GET /api/odontograma/paciente/{pacienteId} : estado vigente de cada pieza. */
  obtenerActual(pacienteId: number): Observable<Odontograma> {
    return this.api.get<Odontograma>(`${this.ruta}/paciente/${pacienteId}`);
  }

  /** GET /api/odontograma/paciente/{pacienteId}/pieza/{numeroPieza} */
  historialDePieza(pacienteId: number, numeroPieza: number): Observable<OdontogramaHistorial> {
    return this.api.get<OdontogramaHistorial>(
      `${this.ruta}/paciente/${pacienteId}/pieza/${numeroPieza}`,
    );
  }

  /** GET /api/odontograma/paciente/{pacienteId}/resumen : conteo por estado. */
  resumenEstados(pacienteId: number): Observable<ResumenOdontograma> {
    return this.api.get<ResumenOdontograma>(`${this.ruta}/paciente/${pacienteId}/resumen`);
  }

  /** POST /api/odontograma : agrega un registro nuevo a la pieza. */
  registrar(datos: OdontogramaRegistroRequest): Observable<OdontogramaPieza> {
    return this.api.post<OdontogramaPieza>(this.ruta, datos);
  }
}
