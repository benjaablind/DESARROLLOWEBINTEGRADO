import { RenderMode, ServerRoute } from '@angular/ssr';

/**
 * Todas las pantallas dependen de datos que la API entrega en tiempo de ejecucion
 * (http://localhost:8080/api), por lo que NO se pueden prerenderizar: durante el
 * build el backend no esta levantado y RenderMode.Prerender fallaria al intentar
 * resolver las rutas. Con RenderMode.Client el servidor entrega el cascaron y el
 * navegador se encarga de pedir los datos ya autenticado.
 */
export const serverRoutes: ServerRoute[] = [
  {
    path: '**',
    renderMode: RenderMode.Client,
  },
];
