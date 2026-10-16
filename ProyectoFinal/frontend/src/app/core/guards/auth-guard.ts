import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from '../services/auth.service';

/**
 * Deja pasar solo si hay sesion con un token vigente.
 * Si no la hay redirige a /login guardando la ruta pedida en el query param
 * `retorno`, para volver alli despues de autenticarse.
 */
export const authGuard: CanActivateFn = (_ruta, estado) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.sesionVigente()) {
    return true;
  }

  return router.createUrlTree(['/login'], {
    queryParams: { retorno: estado.url },
  });
};

/**
 * Impide abrir por URL un modulo que el rol no puede usar (data.modulo de la
 * ruta). El backend igual responderia 403; asi el usuario no ve una pantalla
 * que solo mostraria errores y vuelve al dashboard.
 */
export const permisoGuard: CanActivateFn = (ruta) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const modulo = ruta.data['modulo'] as string | undefined;
  if (!modulo || auth.tienePermiso(modulo)) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
