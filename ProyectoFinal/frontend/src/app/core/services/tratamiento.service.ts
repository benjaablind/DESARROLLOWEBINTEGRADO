import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import {
  EstadoTratamiento,
  FiltroTratamientos,
  SesionEstadoRequest,
  SesionRequest,
  Tratamiento,
  TratamientoRequest,
} from '../models/tratamiento.model';

/** Consume /api/tratamientos, incluidas las sesiones anidadas. */
@Injectable({ providedIn: 'root' })
export class TratamientoService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/tratamientos';

  /** GET /api/tratamientos?pacienteId=&odontologoId=&estado= */
  listar(filtros: FiltroTratamientos = {}): Observable<Tratamiento[]> {
    return this.api.get<Tratamiento[]>(this.ruta, {
      pacienteId: filtros.pacienteId,
      odontologoId: filtros.odontologoId,
      estado: filtros.estado,
    });
  }

  listarPorPaciente(pacienteId: number): Observable<Tratamiento[]> {
    return this.listar({ pacienteId });
  }

  buscarPorId(id: number): Observable<Tratamiento> {
    return this.api.get<Tratamiento>(`${this.ruta}/${id}`);
  }

  crear(datos: TratamientoRequest): Observable<Tratamiento> {
    return this.api.post<Tratamiento>(this.ruta, datos);
  }

  actualizar(id: number, datos: TratamientoRequest): Observable<Tratamiento> {
    return this.api.put<Tratamiento>(`${this.ruta}/${id}`, datos);
  }

  cambiarEstado(id: number, estado: EstadoTratamiento): Observable<Tratamiento> {
    return this.api.patch<Tratamiento>(`${this.ruta}/${id}/estado`, { estado });
  }

  eliminar(id: number): Observable<void> {
    return this.api.delete<void>(`${this.ruta}/${id}`);
  }

  /* ------------------------------- Sesiones ------------------------------ */

  /** POST /api/tratamientos/{id}/sesiones : agrega una sesion al plan. */
  agregarSesion(tratamientoId: number, datos: SesionRequest): Observable<Tratamiento> {
    return this.api.post<Tratamiento>(`${this.ruta}/${tratamientoId}/sesiones`, datos);
  }

  /**
   * PATCH /api/tratamientos/{id}/sesiones/{sesionId}
   * Marca la sesion como realizada o pendiente y actualiza sus observaciones.
   */
  cambiarEstadoSesion(
    tratamientoId: number,
    sesionId: number,
    datos: SesionEstadoRequest,
  ): Observable<Tratamiento> {
    return this.api.patch<Tratamiento>(
      `${this.ruta}/${tratamientoId}/sesiones/${sesionId}`,
      datos,
    );
  }

  eliminarSesion(tratamientoId: number, sesionId: number): Observable<Tratamiento> {
    return this.api.delete<Tratamiento>(`${this.ruta}/${tratamientoId}/sesiones/${sesionId}`);
  }
}
