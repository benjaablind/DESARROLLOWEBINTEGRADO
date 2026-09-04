import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import {
  AntecedentesRequest,
  EstadoPaciente,
  Paciente,
  PacienteRequest,
  PacienteResumen,
} from '../models/paciente.model';

/** Consume /api/pacientes. */
@Injectable({ providedIn: 'root' })
export class PacienteService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/pacientes';

  /** GET /api/pacientes?texto=&estado= : devuelve el listado resumido. */
  listar(texto?: string, estado?: EstadoPaciente): Observable<PacienteResumen[]> {
    return this.api.get<PacienteResumen[]>(this.ruta, { texto, estado });
  }

  /** GET /api/pacientes/{id} : ficha completa. */
  buscarPorId(id: number): Observable<Paciente> {
    return this.api.get<Paciente>(`${this.ruta}/${id}`);
  }

  buscarPorDni(dni: string): Observable<Paciente> {
    return this.api.get<Paciente>(`${this.ruta}/dni/${dni}`);
  }

  crear(datos: PacienteRequest): Observable<Paciente> {
    return this.api.post<Paciente>(this.ruta, datos);
  }

  actualizar(id: number, datos: PacienteRequest): Observable<Paciente> {
    return this.api.put<Paciente>(`${this.ruta}/${id}`, datos);
  }

  actualizarAntecedentes(id: number, datos: AntecedentesRequest): Observable<Paciente> {
    return this.api.patch<Paciente>(`${this.ruta}/${id}/antecedentes`, datos);
  }

  cambiarEstado(id: number, estado: EstadoPaciente): Observable<Paciente> {
    return this.api.patch<Paciente>(`${this.ruta}/${id}/estado`, { estado });
  }

  eliminar(id: number): Observable<void> {
    return this.api.delete<void>(`${this.ruta}/${id}`);
  }
}
