import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';

import { App } from './app';
import { routes } from './app.routes';

/** Matriz de pruebas manuales 11.2, automatizada con las rutas reales. */
describe('App y rutas', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes)],
    }).compileComponents();
  });

  async function navegarA(url: string): Promise<HTMLElement> {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl(url);
    return harness.routeNativeElement as HTMLElement;
  }

  it('el layout tiene la navegacion y el router-outlet', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;
    const enlaces = Array.from(html.querySelectorAll('nav a')).map((a) => a.textContent?.trim());
    expect(enlaces).toEqual(['Inicio', 'Productos', 'Categorías']);
    expect(html.querySelector('router-outlet')).toBeTruthy();
  });

  it('"/" muestra Inicio', async () => {
    const vista = await navegarA('/');
    expect(vista.querySelector('h1')?.textContent).toContain('Navegación y comunicación');
  });

  it('"/productos" muestra una tarjeta por producto', async () => {
    const vista = await navegarA('/productos');
    expect(vista.querySelectorAll('app-producto-card').length).toBe(5);
  });

  it('"/productos/3" muestra Teclado mecánico (parametro de ruta)', async () => {
    const vista = await navegarA('/productos/3');
    expect(vista.querySelector('h1')?.textContent).toContain('Teclado mecánico');
  });

  it('"/productos/999" muestra Producto no encontrado', async () => {
    const vista = await navegarA('/productos/999');
    expect(vista.querySelector('h1')?.textContent).toContain('Producto no encontrado');
  });

  it('"/categorias" muestra la pagina de categorias (Reto A)', async () => {
    const vista = await navegarA('/categorias');
    expect(vista.querySelector('h1')?.textContent).toContain('Categorías');
  });

  it('una ruta inexistente cae en el comodin ** (404)', async () => {
    const vista = await navegarA('/xyz');
    expect(vista.querySelector('h1')?.textContent).toContain('Ruta no encontrada');
  });
});
