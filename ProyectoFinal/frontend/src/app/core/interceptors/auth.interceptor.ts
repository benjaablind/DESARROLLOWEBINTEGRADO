import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

import { environment } from '../../../environments/environment';
import { AuthService } from '../services/auth.service';

/**
 * Agrega "Authorization: Bearer <token>" a cada peticion a la API (semana 9).
 *
 * El login es la unica ruta publica, asi que va sin token. Si el backend
 * responde 401 (token vencido, alterado o firmado con otra clave tras un
 * reinicio), la sesion local se descarta y se vuelve al login conservando
 * la pantalla en la que estaba el usuario.
 */
export const authInterceptor: HttpInterceptorFn = (peticion, siguiente) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const esApi = peticion.url.startsWith(environment.apiUrl);
  const esLogin = peticion.url.endsWith('/auth/login');
  const token = esApi && !esLogin ? auth.token() : null;

  const conToken = token
    ? peticion.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : peticion;

  return siguiente(conToken).pipe(
    catchError((fallo: unknown) => {
      if (fallo instanceof HttpErrorResponse && fallo.status === 401 && esApi && !esLogin) {
        auth.cerrarSesionLocal();
        const actual = router.url;
        if (!actual.startsWith('/login')) {
          router.navigate(['/login'], { queryParams: { retorno: actual, motivo: 'expirada' } });
        }
      }
      return throwError(() => fallo);
    }),
  );
};
