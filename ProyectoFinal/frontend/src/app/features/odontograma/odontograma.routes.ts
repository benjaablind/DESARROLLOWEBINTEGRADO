import { Routes } from '@angular/router';

export const ODONTOGRAMA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/odontograma-view/odontograma-view').then(
        (m) => m.OdontogramaView
      ),
  },
];
