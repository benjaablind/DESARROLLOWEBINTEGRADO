import { HttpErrorResponse, HttpInterceptorFn, HttpResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { tap } from 'rxjs';
import { ContadorSolicitudesService } from './contador-solicitudes.service';

/**
 * Actividad complementaria: interceptor funcional que registra cada
 * solicitud en consola (método, URL, estado y duración) y alimenta el
 * contador de solicitudes exitosas.
 */
export const registroInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const contador = inject(ContadorSolicitudesService);
  const inicio = performance.now();
  const ms = () => Math.round(performance.now() - inicio);

  return siguiente(peticion).pipe(
    tap({
      next: evento => {
        if (evento instanceof HttpResponse) {
          contador.registrarExito();
          console.log(`[HTTP] ${peticion.method} ${peticion.urlWithParams} -> ${evento.status} (${ms()} ms)`);
        }
      },
      error: (fallo: HttpErrorResponse) => {
        console.warn(`[HTTP] ${peticion.method} ${peticion.urlWithParams} -> ${fallo.status} (${ms()} ms)`);
      }
    })
  );
};
