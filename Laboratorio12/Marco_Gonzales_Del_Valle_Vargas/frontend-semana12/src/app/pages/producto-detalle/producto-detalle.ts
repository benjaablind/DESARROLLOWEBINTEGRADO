import { CurrencyPipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { PRODUCTOS } from '../../data/productos.mock';

/**
 * Lee el :id de la URL. snapshot alcanza porque cada detalle se abre con una
 * URL nueva; el parametro llega como texto y se convierte a number antes de
 * buscar.
 */
@Component({
  selector: 'app-producto-detalle',
  imports: [CurrencyPipe, RouterLink],
  templateUrl: './producto-detalle.html',
  styleUrl: './producto-detalle.scss',
})
export class ProductoDetalle {
  private readonly route = inject(ActivatedRoute);

  readonly id = Number(this.route.snapshot.paramMap.get('id'));
  readonly producto = PRODUCTOS.find((item) => item.id === this.id);
}
