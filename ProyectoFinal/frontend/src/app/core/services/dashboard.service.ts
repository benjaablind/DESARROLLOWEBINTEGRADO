import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { ApiService } from './api.service';
import { Dashboard } from '../models/dashboard.model';

/**
 * Consume /api/dashboard.
 *
 * El backend expone un unico endpoint que devuelve todos los indicadores de una
 * sola vez: contadores, proximas citas, pacientes recientes, tratamientos
 * activos, alertas y el resumen de actividad del mes. Traerlo todo junto evita
 * media docena de llamadas al pintar la pantalla principal.
 */
@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly api = inject(ApiService);
  private readonly ruta = '/dashboard';

  /** GET /api/dashboard */
  obtener(): Observable<Dashboard> {
    return this.api.get<Dashboard>(this.ruta);
  }
}
