import { Routes } from '@angular/router';
import { Usuarios } from './usuarios/usuarios';
import { UsuarioDetalle } from './usuarios/usuario-detalle/usuario-detalle';

export const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'usuarios'
  },
  {
    path: 'usuarios',
    title: 'Usuarios',
    component: Usuarios
  },
  {
    // Actividad complementaria: consulta de un usuario con obtenerPorId().
    path: 'usuarios/:id',
    title: 'Detalle de usuario',
    component: UsuarioDetalle
  },
  {
    path: '**',
    redirectTo: 'usuarios'
  }
];
