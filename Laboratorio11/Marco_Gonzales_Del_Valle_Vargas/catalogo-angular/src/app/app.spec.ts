import { TestBed } from '@angular/core/testing';
import { App } from './app';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
    }).compileComponents();
  });

  async function crear() {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    return { fixture, html: fixture.nativeElement as HTMLElement };
  }

  it('crea la aplicacion', () => {
    const fixture = TestBed.createComponent(App);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('muestra el titulo con interpolacion', async () => {
    const { html } = await crear();
    expect(html.querySelector('h1')?.textContent).toContain('Catálogo de productos');
  });

  it('renderiza una tarjeta por producto con @for', async () => {
    const { html } = await crear();
    expect(html.querySelectorAll('.product-card').length).toBe(6);
  });

  it('distingue los productos agotados con [class.out-of-stock] y @if', async () => {
    const { html } = await crear();
    expect(html.querySelectorAll('.product-card.out-of-stock').length).toBe(2);
    expect(html.querySelectorAll('.badge--danger').length).toBe(2);
    expect(html.querySelectorAll('.badge--ok').length).toBe(4);
  });

  it('el boton alterna la visibilidad del stock (event binding + signal)', async () => {
    const { fixture, html } = await crear();
    const boton = html.querySelector('.hero button') as HTMLButtonElement;
    expect(html.querySelectorAll('.stock').length).toBe(6);

    boton.click();
    await fixture.whenStable();
    expect(html.querySelectorAll('.stock').length).toBe(0);
    expect(boton.textContent).toContain('Mostrar stock');

    boton.click();
    await fixture.whenStable();
    expect(html.querySelectorAll('.stock').length).toBe(6);
  });

  it('[(ngModel)] actualiza el texto y filtra la lista', async () => {
    const { fixture, html } = await crear();
    const input = html.querySelector('.search input') as HTMLInputElement;

    input.value = 'mon';
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    expect(html.querySelector('.search__echo')?.textContent).toContain('mon');
    expect(html.querySelectorAll('.product-card').length).toBe(1);
  });

  it('@empty se muestra cuando ningun producto coincide', async () => {
    const { fixture, html } = await crear();
    const input = html.querySelector('.search input') as HTMLInputElement;

    input.value = 'zzz';
    input.dispatchEvent(new Event('input'));
    await fixture.whenStable();

    expect(html.querySelector('.empty')?.textContent).toContain('No hay productos');
  });

  it('muestra las metricas: total y sin stock', async () => {
    const { html } = await crear();
    const valores = Array.from(html.querySelectorAll('.metric__value')).map((e) => e.textContent?.trim());
    expect(valores).toEqual(['6', '30', '2']);
  });
});
