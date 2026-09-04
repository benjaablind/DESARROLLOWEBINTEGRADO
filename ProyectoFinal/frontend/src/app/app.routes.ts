import { Routes } from '@angular/router';
import { MainLayout } from './core/layout/main-layout';
import { PlaceholderPage } from './shared/placeholder-page/placeholder-page';

export const routes: Routes = [
  {
    path: '',
    component: MainLayout,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      {
        path: 'dashboard',
        component: PlaceholderPage,
        data: { titulo: 'Dashboard' },
      },
      {
        path: 'pacientes',
        component: PlaceholderPage,
        data: { titulo: 'Pacientes' },
      },
      {
        path: 'citas',
        component: PlaceholderPage,
        data: { titulo: 'Citas' },
      },
      {
        path: 'historia-clinica',
        loadChildren: () =>
          import('./features/historia-clinica/historia-clinica.routes').then(
            (m) => m.HISTORIA_CLINICA_ROUTES
          ),
      },
      {
        path: 'odontograma',
        loadChildren: () =>
          import('./features/odontograma/odontograma.routes').then(
            (m) => m.ODONTOGRAMA_ROUTES
          ),
      },
      {
        path: 'tratamientos',
        component: PlaceholderPage,
        data: { titulo: 'Tratamientos' },
      },
      {
        path: 'pagos',
        component: PlaceholderPage,
        data: { titulo: 'Pagos' },
      },
      {
        path: 'archivos',
        component: PlaceholderPage,
        data: { titulo: 'Archivos' },
      },
      {
        path: 'reportes',
        component: PlaceholderPage,
        data: { titulo: 'Reportes' },
      },
      {
        path: 'usuarios',
        component: PlaceholderPage,
        data: { titulo: 'Usuarios' },
      },
    ],
  },
  {
    path: 'login',
    component: PlaceholderPage,
    data: { titulo: 'Iniciar sesión' },
  },
  { path: '**', redirectTo: 'dashboard' },
];
