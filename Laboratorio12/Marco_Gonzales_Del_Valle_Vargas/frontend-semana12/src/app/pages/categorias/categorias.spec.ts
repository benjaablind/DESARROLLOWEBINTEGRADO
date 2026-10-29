import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';

import { Categorias } from './categorias';

describe('Categorias (Reto A)', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Categorias],
      providers: [provideRouter([])],
    }).compileComponents();
  });

  it('muestra las categorias unicas de PRODUCTOS, ordenadas', async () => {
    const fixture = TestBed.createComponent(Categorias);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;
    const nombres = Array.from(html.querySelectorAll('.categoria h2')).map((h) => h.textContent);
    expect(nombres).toEqual(['Accesorios', 'Mobiliario', 'Tecnología']);
  });

  it('enlaza cada producto a su detalle', async () => {
    const fixture = TestBed.createComponent(Categorias);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;
    const enlace = html.querySelector('.categoria a') as HTMLAnchorElement;
    expect(enlace.getAttribute('href')).toBe('/productos/3');
  });
});
