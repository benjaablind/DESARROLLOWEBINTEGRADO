import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { Usuarios } from './usuarios';
import { Usuario } from './usuario.model';

const API = 'https://jsonplaceholder.typicode.com/users';

const lista: Usuario[] = [
  { id: 1, name: 'Leanne Graham', username: 'Bret', email: 'Sincere@april.biz', phone: '1-770-736-8031', website: 'hildegard.org' },
  { id: 2, name: 'Ervin Howell', username: 'Antonette', email: 'Shanna@melissa.tv', phone: '010-692-6593', website: 'anastasia.net' }
];

describe('Usuarios', () => {
  let fixture: ComponentFixture<Usuarios>;
  let html: HTMLElement;
  let http: HttpTestingController;

  const filas = () => [...html.querySelectorAll('tbody tr')].map(tr => tr.textContent ?? '');
  const boton = (texto: string, fila = 0) =>
    [...html.querySelectorAll<HTMLButtonElement>('tbody tr')[fila].querySelectorAll('button')]
      .find(b => b.textContent?.includes(texto))!;
  const escribir = (selector: string, valor: string) => {
    const campo = html.querySelector<HTMLInputElement>(selector)!;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
  };

  beforeEach(async () => {
    vi.spyOn(console, 'log').mockImplementation(() => {});
    vi.spyOn(console, 'error').mockImplementation(() => {});
    vi.spyOn(window, 'scrollTo').mockImplementation(() => {});
    await TestBed.configureTestingModule({
      imports: [Usuarios],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    }).compileComponents();

    fixture = TestBed.createComponent(Usuarios);
    html = fixture.nativeElement;
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => {
    http.verify();
    vi.restoreAllMocks();
  });

  it('muestra "Consultando la API..." y luego la tabla con los usuarios del GET', async () => {
    expect(html.textContent).toContain('Consultando la API...');

    http.expectOne(API).flush(lista);
    await fixture.whenStable();

    expect(html.textContent).not.toContain('Consultando la API...');
    expect(filas().length).toBe(2);
  });

  it('muestra el mensaje de error y no se queda cargando cuando el GET falla', async () => {
    http.expectOne(API).flush({}, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();

    expect(html.querySelector('.alerta.error')?.textContent).toContain('No se pudo obtener la lista de usuarios.');
    expect(html.textContent).not.toContain('Consultando la API...');
  });

  it('no envía un formulario inválido', async () => {
    http.expectOne(API).flush(lista);
    html.querySelector<HTMLButtonElement>('button[type=submit]')!.click();
    await fixture.whenStable();

    expect(html.querySelector('.alerta.error')?.textContent).toContain('Revise los campos obligatorios');
    expect(html.querySelectorAll('small').length).toBe(4);
  });

  it('POST agrega el usuario creado al inicio de la tabla', async () => {
    http.expectOne(API).flush(lista);
    escribir('input[formControlName=name]', 'Marco Gonzales');
    escribir('input[formControlName=username]', 'marco');
    escribir('input[formControlName=email]', 'marco@correo.com');
    escribir('input[formControlName=phone]', '987654321');
    html.querySelector<HTMLButtonElement>('button[type=submit]')!.click();

    const req = http.expectOne(API);
    expect(req.request.method).toBe('POST');
    req.flush({ ...req.request.body, id: 11 });
    await fixture.whenStable();

    expect(filas()[0]).toContain('Marco Gonzales');
    expect(html.querySelector('.alerta.exito')?.textContent).toContain('ID recibido: 11');
  });

  it('PUT reemplaza la fila editada', async () => {
    http.expectOne(API).flush(lista);
    await fixture.whenStable();
    boton('Editar').click();
    escribir('input[formControlName=name]', 'Leanne G. Editada');
    html.querySelector<HTMLButtonElement>('button[type=submit]')!.click();

    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('PUT');
    req.flush({ ...req.request.body, id: 1 });
    await fixture.whenStable();

    expect(filas()[0]).toContain('Leanne G. Editada');
  });

  it('PATCH cambia solo el correo de la fila', async () => {
    http.expectOne(API).flush(lista);
    await fixture.whenStable();
    boton('Cambiar correo').click();

    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ email: 'bret@correo-nuevo.com' });
    req.flush({ ...lista[0], email: 'bret@correo-nuevo.com' });
    await fixture.whenStable();

    expect(filas()[0]).toContain('bret@correo-nuevo.com');
    expect(filas()[0]).toContain('Leanne Graham');
  });

  it('DELETE quita la fila tras confirmar', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true);
    http.expectOne(API).flush(lista);
    await fixture.whenStable();
    boton('Eliminar').click();

    const req = http.expectOne(`${API}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush({});
    await fixture.whenStable();

    expect(filas().length).toBe(1);
    expect(filas()[0]).toContain('Ervin Howell');
  });

  it('la búsqueda filtra por nombre o correo sin pedir nada a la API', async () => {
    http.expectOne(API).flush(lista);
    escribir('input[type=search]', 'melissa');
    await fixture.whenStable();

    expect(filas().length).toBe(1);
    expect(filas()[0]).toContain('Ervin Howell');
    expect(html.textContent).toContain('1 de 2 registro(s)');
  });
});
