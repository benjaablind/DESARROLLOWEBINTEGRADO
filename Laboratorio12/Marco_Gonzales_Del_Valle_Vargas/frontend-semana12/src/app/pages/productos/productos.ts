import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';

import { ProductoCard } from '../../components/producto-card/producto-card';
import { PRODUCTOS } from '../../data/productos.mock';

/**
 * Componente padre: entrega cada producto a su tarjeta con [producto] y
 * reacciona a los eventos que la tarjeta emite. La navegacion y el estado de
 * la pagina viven aqui, no en la tarjeta.
 */
@Component({
  selector: 'app-productos',
  imports: [ProductoCard],
  templateUrl: './productos.html',
  styleUrl: './productos.scss',
})
export class Productos {
  private readonly router = inject(Router);

  /** Reto B: se destaca todo producto de mas de S/ 1,000. */
  static readonly PRECIO_DESTACADO = 1000;

  readonly productos = signal(PRODUCTOS);
  readonly favoritos = signal(0);
  /** Reto C: ids agregados a comparacion, sin repetir. */
  readonly comparacion = signal<number[]>([]);
  readonly totalComparacion = computed(() => this.comparacion().length);

  esDestacado(precio: number): boolean {
    return precio > Productos.PRECIO_DESTACADO;
  }

  irDetalle(id: number): void {
    this.router.navigate(['/productos', id]);
  }

  registrarFavorito(id: number): void {
    console.log('Favorito recibido desde el hijo:', id);
    this.favoritos.update((valor) => valor + 1);
  }

  agregarComparacion(id: number): void {
    this.comparacion.update((ids) => (ids.includes(id) ? ids : [...ids, id]));
  }
}
