import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Deja pasar solo si hay sesion activa.
 * Si no la hay redirige a /login guardando la ruta pedida en el query param
 * `retorno`, para volver alli despues de autenticarse.
 */
export const authGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.estaAutenticado()) {
    return true;
  }

  return router.createUrlTree(['/login'], {
    queryParams: { retorno: estado.url },
  });
};
