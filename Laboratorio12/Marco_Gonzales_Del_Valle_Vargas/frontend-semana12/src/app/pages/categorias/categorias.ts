import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

import { PRODUCTOS } from '../../data/productos.mock';

interface Categoria {
  nombre: string;
  productos: { id: number; nombre: string }[];
}

/** Reto A: categorias unicas de PRODUCTOS, con los productos de cada una. */
@Component({
  selector: 'app-categorias',
  imports: [RouterLink],
  templateUrl: './categorias.html',
  styleUrl: './categorias.scss',
})
export class Categorias {
  readonly categorias: Categoria[] = [...new Set(PRODUCTOS.map((p) => p.categoria))]
    .sort((a, b) => a.localeCompare(b))
    .map((nombre) => ({
      nombre,
      productos: PRODUCTOS.filter((p) => p.categoria === nombre).map(({ id, nombre }) => ({ id, nombre })),
    }));
}
