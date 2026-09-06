import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';

/** Parametros de consulta admitidos. Los valores nulos se descartan. */
export type ParamsHttp = Record<string, string | number | boolean | null | undefined>;

/**
 * Envoltura unica sobre HttpClient.
 * Arma la URL a partir de environment.apiUrl y traduce cualquier fallo a un
 * objeto ErrorApi para que los componentes solo tengan que leer `.mensaje`.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  readonly baseUrl = environment.apiUrl;

  get<T>(ruta: string, params?: ParamsHttp): Observable<T> {
    return this.http
      .get<T>(this.url(ruta), { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  post<T>(ruta: string, cuerpo?: unknown, params?: ParamsHttp): Observable<T> {
    return this.http
      .post<T>(this.url(ruta), cuerpo ?? {}, { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  put<T>(ruta: string, cuerpo?: unknown, params?: ParamsHttp): Observable<T> {
    return this.http
      .put<T>(this.url(ruta), cuerpo ?? {}, { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  patch<T>(ruta: string, cuerpo?: unknown, params?: ParamsHttp): Observable<T> {
    return this.http
      .patch<T>(this.url(ruta), cuerpo ?? {}, { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  delete<T>(ruta: string, params?: ParamsHttp): Observable<T> {
    return this.http
      .delete<T>(this.url(ruta), { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  /** POST de un FormData: no se fija Content-Type para que el navegador ponga el boundary. */
  postFormData<T>(ruta: string, datos: FormData, params?: ParamsHttp): Observable<T> {
    return this.http
      .post<T>(this.url(ruta), datos, { params: this.armarParams(params) })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  /** Descarga un recurso binario como Blob. */
  getBlob(ruta: string, params?: ParamsHttp): Observable<Blob> {
    return this.http
      .get(this.url(ruta), { params: this.armarParams(params), responseType: 'blob' })
      .pipe(catchError((e) => this.traducirError(e, ruta)));
  }

  /** Construye la URL absoluta a partir de una ruta relativa como "/pacientes/1". */
  url(ruta: string): string {
    const limpia = ruta.startsWith('/') ? ruta : `/${ruta}`;
    return `${this.baseUrl}${limpia}`;
  }

  private armarParams(params?: ParamsHttp): HttpParams {
    let http = new HttpParams();
    if (!params) {
      return http;
    }
    for (const [clave, valor] of Object.entries(params)) {
      if (valor !== null && valor !== undefined && valor !== '') {
        http = http.set(clave, String(valor));
      }
    }
    return http;
  }

  /**
   * Normaliza cualquier fallo a ErrorApi.
   * Si el backend respondio con su JSON de error se respeta su mensaje;
   * si no hubo respuesta (estado 0) se avisa que el servidor no esta disponible.
   */
  private traducirError(fallo: HttpErrorResponse, ruta: string): Observable<never> {
    const ahora = new Date().toISOString().slice(0, 19);

    if (fallo.status === 0) {
      return throwError(
        () =>
          ({
            estado: 0,
            mensaje: 'No se pudo conectar con el servidor',
            ruta,
            fechaHora: ahora,
          }) satisfies ErrorApi,
      );
    }

    const cuerpo = fallo.error as Partial<ErrorApi> | string | null;

    if (cuerpo && typeof cuerpo === 'object' && typeof cuerpo.mensaje === 'string') {
      return throwError(
        () =>
          ({
            estado: cuerpo.estado ?? fallo.status,
            mensaje: cuerpo.mensaje as string,
            ruta: cuerpo.ruta ?? ruta,
            fechaHora: cuerpo.fechaHora ?? ahora,
          }) satisfies ErrorApi,
      );
    }

    return throwError(
      () =>
        ({
          estado: fallo.status,
          mensaje:
            typeof cuerpo === 'string' && cuerpo.trim().length > 0
              ? cuerpo
              : this.mensajePorEstado(fallo.status),
          ruta,
          fechaHora: ahora,
        }) satisfies ErrorApi,
    );
  }

  private mensajePorEstado(estado: number): string {
    switch (estado) {
      case 400:
        return 'Los datos enviados no son validos';
      case 401:
        return 'Debe iniciar sesion para continuar';
      case 403:
        return 'No tiene permisos para realizar esta accion';
      case 404:
        return 'El recurso solicitado no existe';
      case 409:
        return 'La operacion entra en conflicto con los datos actuales';
      case 500:
        return 'Ocurrio un error interno en el servidor';
      default:
        return 'Ocurrio un error inesperado';
    }
  }
}
