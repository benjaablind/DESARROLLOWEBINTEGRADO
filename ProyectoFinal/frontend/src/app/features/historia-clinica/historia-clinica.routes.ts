import { Routes } from '@angular/router';

export const HISTORIA_CLINICA_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./pages/historia-clinica-list/historia-clinica-list').then(
        (m) => m.HistoriaClinicaList
      ),
  },
  {
    path: 'nueva',
    loadComponent: () =>
      import('./pages/historia-clinica-nueva/historia-clinica-nueva').then(
        (m) => m.HistoriaClinicaNueva
      ),
  },
  {
    path: ':id',
    loadComponent: () =>
      import('./pages/historia-clinica-detalle/historia-clinica-detalle').then(
        (m) => m.HistoriaClinicaDetalle
      ),
  },
];
