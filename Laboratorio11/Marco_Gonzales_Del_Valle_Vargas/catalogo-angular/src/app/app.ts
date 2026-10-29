import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

import { SummaryPanel } from './features/summary-panel/summary-panel';
import { AppFooter } from './shared/app-footer/app-footer';
import { AppHeader } from './shared/app-header/app-header';

interface Producto {
  id: number;
  nombre: string;
  precio: number;
  stock: number;
}

/**
 * Componente raiz: el panel de productos de la semana 11.
 * Los datos son locales; el consumo del API Spring Boot llega en la semana 14.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AppHeader, SummaryPanel, AppFooter, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly titulo = 'Catálogo de productos';
  protected readonly mostrarStock = signal(true);

  /** Mini practica 9: enlace bidireccional con [(ngModel)]. */
  protected busqueda = '';

  protected readonly productos: Producto[] = [
    { id: 1, nombre: 'Laptop', precio: 3500, stock: 8 },
    { id: 2, nombre: 'Teclado mecánico', precio: 250, stock: 0 },
    { id: 3, nombre: 'Monitor 27 pulgadas', precio: 980, stock: 4 },
    { id: 4, nombre: 'Mouse inalámbrico', precio: 120, stock: 15 },
    { id: 5, nombre: 'Audífonos con micrófono', precio: 280, stock: 0 },
    { id: 6, nombre: 'Webcam HD', precio: 260, stock: 3 },
  ];

  /** Metricas del panel (extension 12.2). */
  protected readonly totalProductos = this.productos.length;
  protected readonly totalSinStock = this.productos.filter((p) => p.stock === 0).length;
  protected readonly totalUnidades = this.productos.reduce((suma, p) => suma + p.stock, 0);

  /** Productos cuyo nombre contiene el texto escrito en el buscador. */
  protected get productosFiltrados(): Producto[] {
    const texto = this.busqueda.trim().toLowerCase();
    return texto ? this.productos.filter((p) => p.nombre.toLowerCase().includes(texto)) : this.productos;
  }

  protected alternarStock(): void {
    this.mostrarStock.update((valor) => !valor);
  }
}
