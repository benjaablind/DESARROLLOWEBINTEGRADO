import { CurrencyPipe } from '@angular/common';
import { Component, input, output } from '@angular/core';

import { Producto } from '../../models/producto';

/**
 * Tarjeta reutilizable: solo presenta un producto y emite eventos.
 * No navega ni guarda estado: el padre decide que hacer con cada evento.
 */
@Component({
  selector: 'app-producto-card',
  imports: [CurrencyPipe],
  templateUrl: './producto-card.html',
  styleUrl: './producto-card.scss',
})
export class ProductoCard {
  /** Padre -> hijo: obligatorio; Angular lo verifica al compilar. */
  readonly producto = input.required<Producto>();
  /** Reto B: opcional, con valor por defecto. */
  readonly destacado = input(false);

  /** Hijo -> padre: cada evento lleva el id del producto. */
  readonly verDetalle = output<number>();
  readonly favorito = output<number>();
  /** Reto C. */
  readonly agregarComparacion = output<number>();

  solicitarDetalle(): void {
    this.verDetalle.emit(this.producto().id);
  }

  marcarFavorito(): void {
    this.favorito.emit(this.producto().id);
  }

  comparar(): void {
    this.agregarComparacion.emit(this.producto().id);
  }
}
