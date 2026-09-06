import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { Archivo, FiltroArchivos, TipoArchivo } from '../models/archivo.model';

/** Consume /api/archivos. */
@Injectable({ providedIn: 'root' })
export class ArchivoService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/archivos';

  /** GET /api/archivos con filtros opcionales. */
  listar(filtros: FiltroArchivos = {}): Observable<Archivo[]> {
    return this.api.get<Archivo[]>(this.ruta, {
      pacienteId: filtros.pacienteId,
      tipo: filtros.tipo,
      historiaClinicaId: filtros.historiaClinicaId,
    });
  }

  /** GET /api/archivos/paciente/{pacienteId} */
  listarPorPaciente(pacienteId: number, tipo?: TipoArchivo): Observable<Archivo[]> {
    return this.api.get<Archivo[]>(`${this.ruta}/paciente/${pacienteId}`, { tipo });
  }

  buscarPorId(id: number): Observable<Archivo> {
    return this.api.get<Archivo>(`${this.ruta}/${id}`);
  }

  /**
   * POST /api/archivos con multipart/form-data.
   * El FormData debe traer al menos: archivo (File), pacienteId, usuarioId y tipo.
   */
  subir(datos: FormData): Observable<Archivo> {
    return this.api.postFormData<Archivo>(this.ruta, datos);
  }

  /**
   * Arma el FormData esperado por el backend a partir de los datos del formulario.
   */
  construirFormData(entrada: {
    archivo: File;
    pacienteId: number;
    usuarioId: number;
    tipo: TipoArchivo;
    descripcion?: string | null;
    historiaClinicaId?: number | null;
  }): FormData {
    const datos = new FormData();
    datos.append('archivo', entrada.archivo, entrada.archivo.name);
    datos.append('pacienteId', String(entrada.pacienteId));
    datos.append('usuarioId', String(entrada.usuarioId));
    datos.append('tipo', entrada.tipo);
    if (entrada.descripcion) {
      datos.append('descripcion', entrada.descripcion);
    }
    if (entrada.historiaClinicaId !== null && entrada.historiaClinicaId !== undefined) {
      datos.append('historiaClinicaId', String(entrada.historiaClinicaId));
    }
    return datos;
  }

  /** URL absoluta de descarga, apta para un href o para el src de una imagen. */
  urlDescarga(id: number): string {
    return this.api.url(`${this.ruta}/${id}/descargar`);
  }

  /** Descarga el binario cuando se necesita manipularlo en el navegador. */
  descargar(id: number): Observable<Blob> {
    return this.api.getBlob(`${this.ruta}/${id}/descargar`);
  }

  /**
   * PUT /api/archivos/{id}
   * Solo actualiza los metadatos. El binario ya subido nunca se reemplaza.
   */
  actualizar(
    id: number,
    datos: { tipo: TipoArchivo; descripcion?: string | null; historiaClinicaId?: number | null },
  ): Observable<Archivo> {
    return this.api.put<Archivo>(`${this.ruta}/${id}`, datos);
  }

  eliminar(id: number): Observable<void> {
    return this.api.delete<void>(`${this.ruta}/${id}`);
  }
}
