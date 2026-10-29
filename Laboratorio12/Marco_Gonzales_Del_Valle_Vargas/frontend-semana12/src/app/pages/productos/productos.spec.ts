import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { vi } from 'vitest';

import { Productos } from './productos';

describe('Productos (padre)', () => {
  let fixture: ComponentFixture<Productos>;
  let html: HTMLElement;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Productos],
      providers: [provideRouter([])],
    }).compileComponents();
    fixture = TestBed.createComponent(Productos);
    await fixture.whenStable();
    html = fixture.nativeElement as HTMLElement;
  });

  function botonDe(tarjeta: number, texto: string): HTMLButtonElement {
    const card = html.querySelectorAll('app-producto-card')[tarjeta];
    return Array.from(card.querySelectorAll('button')).find((b) => b.textContent?.includes(texto))!;
  }

  it('entrega cada producto a su tarjeta con [producto]', () => {
    const nombres = Array.from(html.querySelectorAll('app-producto-card h2')).map((h) => h.textContent);
    expect(nombres[0]).toContain('Laptop Pro');
    expect(nombres.length).toBe(5);
  });

  it('marca como destacados solo los de mas de S/ 1,000 (Reto B)', () => {
    const destacados = Array.from(html.querySelectorAll('.card.destacado h2')).map((h) => h.textContent?.trim());
    expect(destacados).toEqual(['Laptop Pro', 'Monitor 27 pulgadas']);
  });

  it('cuenta los favoritos que emiten las tarjetas: dos clics, contador 2', async () => {
    const log = vi.spyOn(console, 'log').mockImplementation(() => {});
    botonDe(0, 'Favorito').click();
    botonDe(0, 'Favorito').click();
    await fixture.whenStable();
    expect(html.querySelector('.favoritos')?.textContent).toBe('2');
    expect(log).toHaveBeenCalledWith('Favorito recibido desde el hijo:', 1);
    log.mockRestore();
  });

  it('cuenta los productos en comparacion sin repetir (Reto C)', async () => {
    botonDe(0, 'Comparar').click();
    botonDe(0, 'Comparar').click();
    botonDe(2, 'Comparar').click();
    await fixture.whenStable();
    expect(html.querySelector('.comparacion')?.textContent).toBe('2');
  });

  it('navega al detalle con Router.navigate al recibir verDetalle', () => {
    const router = TestBed.inject(Router);
    const navegar = vi.spyOn(router, 'navigate').mockResolvedValue(true);
    botonDe(1, 'Ver detalle').click();
    expect(navegar).toHaveBeenCalledWith(['/productos', 2]);
  });
});
