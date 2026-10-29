import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Producto } from '../../models/producto';
import { ProductoCard } from './producto-card';

const MONITOR: Producto = {
  id: 2,
  nombre: 'Monitor 27 pulgadas',
  categoria: 'Tecnología',
  precio: 1199,
  descripcion: 'Monitor QHD para trabajo profesional.',
};

describe('ProductoCard', () => {
  let fixture: ComponentFixture<ProductoCard>;
  let html: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [ProductoCard] }).compileComponents();
    fixture = TestBed.createComponent(ProductoCard);
    // input.required: sin producto el componente no puede renderizarse.
    fixture.componentRef.setInput('producto', MONITOR);
    await fixture.whenStable();
    html = fixture.nativeElement as HTMLElement;
  });

  function boton(texto: string): HTMLButtonElement {
    return Array.from(html.querySelectorAll('button')).find((b) => b.textContent?.includes(texto))!;
  }

  it('renderiza el producto recibido con input()', () => {
    expect(html.querySelector('h2')?.textContent).toContain('Monitor 27 pulgadas');
    expect(html.querySelector('.badge')?.textContent).toContain('Tecnología');
    expect(html.querySelector('strong')?.textContent).toContain('1,199.00');
  });

  it('no esta destacado por defecto (Reto B)', () => {
    expect(html.querySelector('.card.destacado')).toBeNull();
  });

  it('aplica la clase destacado cuando el padre lo indica (Reto B)', async () => {
    fixture.componentRef.setInput('destacado', true);
    await fixture.whenStable();
    expect(html.querySelector('.card.destacado')).toBeTruthy();
    expect(html.querySelector('.ribbon')?.textContent).toContain('Destacado');
  });

  it('emite verDetalle, favorito y agregarComparacion con el id', () => {
    const emitidos: string[] = [];
    fixture.componentInstance.verDetalle.subscribe((id) => emitidos.push(`detalle:${id}`));
    fixture.componentInstance.favorito.subscribe((id) => emitidos.push(`favorito:${id}`));
    fixture.componentInstance.agregarComparacion.subscribe((id) => emitidos.push(`comparar:${id}`));

    boton('Ver detalle').click();
    boton('Favorito').click();
    boton('Comparar').click();

    expect(emitidos).toEqual(['detalle:2', 'favorito:2', 'comparar:2']);
  });
});
