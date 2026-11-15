import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { App } from './app';
import { routes } from './app.routes';

const API = 'https://jsonplaceholder.typicode.com/users';

describe('App', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    vi.spyOn(console, 'error').mockImplementation(() => {});
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [
        provideRouter(routes, withComponentInputBinding()),
        provideHttpClient(),
        provideHttpClientTesting()
      ]
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => vi.restoreAllMocks());

  it('muestra la cabecera con el contador y el enlace a usuarios', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();
    const html = fixture.nativeElement as HTMLElement;

    expect(html.textContent).toContain('Servicios y consumo de APIs');
    expect(html.textContent).toContain('Solicitudes exitosas: 0');
    expect(html.querySelector('nav a')?.getAttribute('href')).toBe('/usuarios');
  });

  it('/usuarios/:id consulta un usuario con obtenerPorId()', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/usuarios/3');

    http.expectOne(`${API}/3`).flush({
      id: 3, name: 'Clementine Bauch', username: 'Samantha',
      email: 'Nathan@yesenia.net', phone: '1-463-123-4447', website: 'ramiro.info'
    });
    harness.detectChanges();

    expect(harness.routeNativeElement?.textContent).toContain('Clementine Bauch');
  });

  it('/usuarios/999 informa que el usuario no existe', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/usuarios/999');

    http.expectOne(`${API}/999`).flush({}, { status: 404, statusText: 'Not Found' });
    harness.detectChanges();

    expect(harness.routeNativeElement?.textContent).toContain('No existe un usuario con ID 999.');
  });

  it('las rutas desconocidas redirigen a /usuarios', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/xyz');
    http.expectOne(API).flush([]);
    harness.detectChanges();

    expect(harness.routeNativeElement?.textContent).toContain('Gestión de usuarios');
  });
});
