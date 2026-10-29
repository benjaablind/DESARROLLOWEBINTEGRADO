import { Routes } from '@angular/router';

import { Categorias } from './pages/categorias/categorias';
import { Inicio } from './pages/inicio/inicio';
import { NoEncontrado } from './pages/no-encontrado/no-encontrado';
import { ProductoDetalle } from './pages/producto-detalle/producto-detalle';
import { Productos } from './pages/productos/productos';

/**
 * Mapa URL -> componente. El router prueba las rutas en orden, por eso el
 * comodin ** va al final: si estuviera primero, atraparia todas las URLs.
 */
export const routes: Routes = [
  { path: '', component: Inicio, title: 'Inicio · DevStore' },
  { path: 'productos', component: Productos, title: 'Productos · DevStore' },
  { path: 'productos/:id', component: ProductoDetalle, title: 'Detalle · DevStore' },
  { path: 'categorias', component: Categorias, title: 'Categorías · DevStore' },
  { path: '**', component: NoEncontrado, title: 'No encontrado · DevStore' },
];
