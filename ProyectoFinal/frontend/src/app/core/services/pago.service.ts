import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { EstadoCuenta, FiltroPagos, Pago, PagoRequest } from '../models/pago.model';

/** Consume /api/pagos. */
@Injectable({ providedIn: 'root' })
export class PagoService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/pagos';

  /** GET /api/pagos con filtros opcionales. */
  listar(filtros: FiltroPagos = {}): Observable<Pago[]> {
    return this.api.get<Pago[]>(this.ruta, {
      pacienteId: filtros.pacienteId,
      tratamientoId: filtros.tratamientoId,
      metodo: filtros.metodo,
      desde: filtros.desde,
      hasta: filtros.hasta,
    });
  }

  buscarPorId(id: number): Observable<Pago> {
    return this.api.get<Pago>(`${this.ruta}/${id}`);
  }

  /** GET /api/pagos/tratamiento/{tratamientoId} */
  listarPorTratamiento(tratamientoId: number): Observable<Pago[]> {
    return this.api.get<Pago[]>(`${this.ruta}/tratamiento/${tratamientoId}`);
  }

  /** GET /api/pagos?pacienteId= : el backend filtra por parametro, no por ruta. */
  listarPorPaciente(pacienteId: number): Observable<Pago[]> {
    return this.listar({ pacienteId });
  }

  /** GET /api/pagos/estado-cuenta/{pacienteId} : totales y saldo del paciente. */
  estadoCuenta(pacienteId: number): Observable<EstadoCuenta> {
    return this.api.get<EstadoCuenta>(`${this.ruta}/estado-cuenta/${pacienteId}`);
  }

  registrar(datos: PagoRequest): Observable<Pago> {
    return this.api.post<Pago>(this.ruta, datos);
  }

  anular(id: number): Observable<void> {
    return this.api.delete<void>(`${this.ruta}/${id}`);
  }
}
