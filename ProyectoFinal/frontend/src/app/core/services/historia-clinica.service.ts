import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { HistoriaClinica, HistoriaClinicaRequest } from '../models/historia.model';

/** Consume /api/historias. */
@Injectable({ providedIn: 'root' })
export class HistoriaClinicaService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/historias';

  /** GET /api/historias/paciente/{pacienteId} : atenciones ordenadas por fecha. */
  listarPorPaciente(pacienteId: number): Observable<HistoriaClinica[]> {
    return this.api.get<HistoriaClinica[]>(`${this.ruta}/paciente/${pacienteId}`);
  }

  buscarPorId(id: number): Observable<HistoriaClinica> {
    return this.api.get<HistoriaClinica>(`${this.ruta}/${id}`);
  }

  crear(datos: HistoriaClinicaRequest): Observable<HistoriaClinica> {
    return this.api.post<HistoriaClinica>(this.ruta, datos);
  }

  /** PATCH /api/historias/{id}/anular : las historias no se borran, se anulan. */
  anular(id: number, motivo: string): Observable<HistoriaClinica> {
    return this.api.patch<HistoriaClinica>(`${this.ruta}/${id}/anular`, { motivo });
  }
}
