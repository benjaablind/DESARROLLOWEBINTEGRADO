import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import {
  Cita,
  CitaEstadoRequest,
  CitaReprogramarRequest,
  CitaRequest,
  EstadoCita,
  FiltroCitas,
} from '../models/cita.model';

/** Consume /api/citas. */
@Injectable({ providedIn: 'root' })
export class CitaService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/citas';

  /** GET /api/citas con filtros opcionales y combinables. */
  listar(filtros: FiltroCitas = {}): Observable<Cita[]> {
    return this.api.get<Cita[]>(this.ruta, {
      dia: filtros.dia,
      pacienteId: filtros.pacienteId,
      odontologoId: filtros.odontologoId,
      estado: filtros.estado,
    });
  }

  /** GET /api/citas/agenda?dia=YYYY-MM-DD */
  agenda(dia: string): Observable<Cita[]> {
    return this.api.get<Cita[]>(`${this.ruta}/agenda`, { dia });
  }

  /** GET /api/citas/disponibilidad : horas ya ocupadas del odontologo ese dia. */
  disponibilidad(odontologoId: number, dia: string): Observable<string[]> {
    return this.api.get<string[]>(`${this.ruta}/disponibilidad`, { odontologoId, dia });
  }

  buscarPorId(id: number): Observable<Cita> {
    return this.api.get<Cita>(`${this.ruta}/${id}`);
  }

  crear(datos: CitaRequest): Observable<Cita> {
    return this.api.post<Cita>(this.ruta, datos);
  }

  actualizar(id: number, datos: CitaRequest): Observable<Cita> {
    return this.api.put<Cita>(`${this.ruta}/${id}`, datos);
  }

  cambiarEstado(id: number, datos: CitaEstadoRequest): Observable<Cita> {
    return this.api.patch<Cita>(`${this.ruta}/${id}/estado`, datos);
  }

  confirmar(id: number): Observable<Cita> {
    return this.api.patch<Cita>(`${this.ruta}/${id}/confirmar`);
  }

  /** PATCH /api/citas/{id}/asistencia : marca ATENDIDA o NO_ASISTIO. */
  registrarAsistencia(id: number, asistio: boolean): Observable<Cita> {
    return this.api.patch<Cita>(`${this.ruta}/${id}/asistencia`, { asistio });
  }

  cancelar(id: number, motivo?: string): Observable<Cita> {
    return this.api.patch<Cita>(`${this.ruta}/${id}/cancelar`, { motivo: motivo ?? null });
  }

  reprogramar(id: number, datos: CitaReprogramarRequest): Observable<Cita> {
    return this.api.post<Cita>(`${this.ruta}/${id}/reprogramar`, datos);
  }

  eliminar(id: number): Observable<void> {
    return this.api.delete<void>(`${this.ruta}/${id}`);
  }

  /** Atajo: citas de un paciente en cualquier estado. */
  listarPorPaciente(pacienteId: number, estado?: EstadoCita): Observable<Cita[]> {
    return this.listar({ pacienteId, estado });
  }
}
