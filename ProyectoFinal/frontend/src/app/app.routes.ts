import { Routes } from '@angular/router';

import { authGuard, permisoGuard } from './core/guards/auth-guard';

/**
 * Ruteo de la aplicacion.
 * Todo cuelga de una ruta padre protegida que monta el LayoutComponent;
 * el login es la unica pantalla que queda fuera del cascaron. authGuard exige
 * un token vigente y permisoGuard, que el rol pueda usar el modulo (data.modulo).
 */
export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'dashboard',
  },
  {
    path: 'login',
    title: 'Iniciar sesion - Clinica Dental',
    loadComponent: () => import('./paginas/login/login').then((m) => m.LoginComponent),
  },
  {
    path: '',
    canActivate: [authGuard],
    canActivateChild: [permisoGuard],
    loadComponent: () => import('./layout/layout').then((m) => m.LayoutComponent),
    children: [
      {
        path: 'dashboard',
        data: { modulo: 'dashboard' },
        title: 'Dashboard - Clinica Dental',
        loadComponent: () =>
          import('./paginas/dashboard/dashboard').then((m) => m.DashboardComponent),
      },
      {
        path: 'pacientes',
        data: { modulo: 'pacientes' },
        title: 'Pacientes - Clinica Dental',
        loadComponent: () =>
          import('./paginas/pacientes/pacientes').then((m) => m.PacientesComponent),
      },
      {
        path: 'pacientes/:id',
        data: { modulo: 'pacientes' },
        title: 'Ficha del paciente - Clinica Dental',
        loadComponent: () =>
          import('./paginas/pacientes/paciente-detalle').then((m) => m.PacienteDetalleComponent),
      },
      {
        path: 'citas',
        data: { modulo: 'citas' },
        title: 'Citas - Clinica Dental',
        loadComponent: () => import('./paginas/citas/citas').then((m) => m.CitasComponent),
      },
      {
        path: 'historias',
        data: { modulo: 'historias' },
        title: 'Historias clinicas - Clinica Dental',
        loadComponent: () =>
          import('./paginas/historias/historias').then((m) => m.HistoriasComponent),
      },
      {
        path: 'odontograma',
        data: { modulo: 'odontograma' },
        title: 'Odontograma - Clinica Dental',
        loadComponent: () =>
          import('./paginas/odontograma/odontograma').then((m) => m.OdontogramaComponent),
      },
      {
        path: 'tratamientos',
        data: { modulo: 'tratamientos' },
        title: 'Tratamientos - Clinica Dental',
        loadComponent: () =>
          import('./paginas/tratamientos/tratamientos').then((m) => m.TratamientosComponent),
      },
      {
        path: 'pagos',
        data: { modulo: 'pagos' },
        title: 'Pagos - Clinica Dental',
        loadComponent: () => import('./paginas/pagos/pagos').then((m) => m.PagosComponent),
      },
      {
        path: 'archivos',
        data: { modulo: 'archivos' },
        title: 'Archivos - Clinica Dental',
        loadComponent: () =>
          import('./paginas/archivos/archivos').then((m) => m.ArchivosComponent),
      },
      {
        path: 'reportes',
        data: { modulo: 'reportes' },
        title: 'Reportes - Clinica Dental',
        loadComponent: () =>
          import('./paginas/reportes/reportes').then((m) => m.ReportesComponent),
      },
      {
        path: 'usuarios',
        data: { modulo: 'usuarios' },
        title: 'Usuarios - Clinica Dental',
        loadComponent: () =>
          import('./paginas/usuarios/usuarios').then((m) => m.UsuariosComponent),
      },
    ],
  },
  {
    path: '**',
    title: 'Pagina no encontrada - Clinica Dental',
    loadComponent: () =>
      import('./paginas/no-encontrada/no-encontrada').then((m) => m.NoEncontradaComponent),
  },
];
