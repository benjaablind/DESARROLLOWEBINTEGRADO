import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { App } from './app';
import { routes } from './app.routes';

describe('App', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideRouter(routes)]
    }).compileComponents();
  });

  it('muestra la cabecera con los dos enlaces de navegación', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.querySelector('h1')?.textContent).toContain('Laboratorio de formularios');
    const enlaces = [...html.querySelectorAll('nav a')].map(a => a.getAttribute('href'));
    expect(enlaces).toEqual(['/plantilla', '/reactivo']);
  });

  it('redirige la raíz y las rutas inexistentes al formulario de plantilla', async () => {
    const harness = await RouterTestingHarness.create();

    await harness.navigateByUrl('/');
    expect(harness.routeNativeElement?.textContent).toContain('Formulario de contacto');

    await harness.navigateByUrl('/no-existe');
    expect(harness.routeNativeElement?.textContent).toContain('Formulario de contacto');
  });

  it('carga el formulario reactivo de forma diferida en /reactivo', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/reactivo');
    expect(harness.routeNativeElement?.textContent).toContain('Formulario de registro');
  });
});
