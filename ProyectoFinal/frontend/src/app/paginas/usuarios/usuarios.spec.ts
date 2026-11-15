import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { Usuario } from '../../core/models';
import { UsuariosComponent } from './usuarios';

const URL = `${environment.apiUrl}/usuarios`;

const rdiaz: Usuario = {
  id: 3,
  nombres: 'Rosa',
  apellidos: 'Diaz',
  nombreCompleto: 'Rosa Diaz',
  email: 'rdiaz@clinica.pe',
  usuario: 'rdiaz',
  rol: 'RECEPCIONISTA',
  activo: true,
  fechaRegistro: '2026-03-01T08:00:00',
};

describe('UsuariosComponent (formularios)', () => {
  let fixture: ComponentFixture<UsuariosComponent>;
  let http: HttpTestingController;
  let raiz: HTMLElement;

  const refrescar = async () => {
    fixture.detectChanges();
    await fixture.whenStable();
  };

  const boton = (texto: string): HTMLButtonElement => {
    const encontrado = Array.from(raiz.querySelectorAll('button')).find(
      (b) => b.textContent?.trim() === texto,
    );
    if (!encontrado) {
      throw new Error(`No hay un boton "${texto}"`);
    }
    return encontrado;
  };

  const escribir = (id: string, valor: string) => {
    const campo = raiz.querySelector<HTMLInputElement>(`#${id}`)!;
    campo.value = valor;
    campo.dispatchEvent(new Event('input'));
    campo.dispatchEvent(new Event('blur'));
  };

  const enviar = async () => {
    raiz.querySelector('aside form')!.dispatchEvent(new Event('submit'));
    await refrescar();
  };

  const errores = () =>
    Array.from(raiz.querySelectorAll('.form-error')).map((e) =>
      (e.textContent ?? '').replace(/\s+/g, ' ').trim(),
    );

  /** Rellena el alta con datos validos; cada prueba cambia lo que necesita. */
  const llenarAlta = () => {
    escribir('nombres', '  Luis ');
    escribir('apellidos', 'Ramos');
    escribir('email', 'lramos@clinica.pe');
    escribir('usuario', ' lramos ');
    escribir('password', 'lramos123');
    escribir('confirmarPassword', 'lramos123');
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UsuariosComponent],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(UsuariosComponent);
    raiz = fixture.nativeElement as HTMLElement;
    await refrescar();

    // El constructor carga el listado.
    http.expectOne(URL).flush([rdiaz]);
    await refrescar();
  });

  afterEach(() => http.verify());

  it('no muestra errores al abrir el alta y los muestra todos al intentar enviar', async () => {
    boton('Nuevo usuario').click();
    await refrescar();
    expect(errores()).toEqual([]);

    await enviar();

    expect(errores()).toEqual([
      'Los nombres son obligatorios.',
      'Los apellidos son obligatorios.',
      'El correo es obligatorio.',
      'El usuario es obligatorio.',
      'La contraseña es obligatoria.',
      'Repita la contraseña.',
    ]);
    http.expectNone(URL);
  });

  it('rechaza nombres en blanco, usuario corto tras recortar y confirmacion distinta', async () => {
    boton('Nuevo usuario').click();
    await refrescar();
    llenarAlta();
    escribir('nombres', '    ');
    escribir('usuario', ' abc ');
    escribir('confirmarPassword', 'lramos124');
    await enviar();

    expect(errores()).toEqual([
      'Los nombres no pueden contener solo espacios.',
      'El usuario debe tener entre 4 y 30 caracteres, sin contar los espacios de los extremos.',
      'Las contraseñas no coinciden.',
    ]);
    http.expectNone(URL);
  });

  it('con datos validos envia el UsuarioRequest recortado y sin la confirmacion', async () => {
    boton('Nuevo usuario').click();
    await refrescar();
    llenarAlta();
    await enviar();

    const req = http.expectOne((r) => r.method === 'POST' && r.url === URL);
    expect(req.request.body).toEqual({
      nombres: 'Luis',
      apellidos: 'Ramos',
      email: 'lramos@clinica.pe',
      usuario: 'lramos',
      password: 'lramos123',
      rol: 'RECEPCIONISTA',
    });
    req.flush({ ...rdiaz, id: 4, usuario: 'lramos' }, { status: 201, statusText: 'Created' });
    await refrescar();

    // Tras crear se cierra el panel y se recarga el listado.
    http.expectOne((r) => r.method === 'GET' && r.url === URL).flush([rdiaz]);
    await refrescar();
    expect(raiz.querySelector('aside')).toBeNull();
  });

  it('si el backend rechaza el alta, finalize() libera el boton y se ve el mensaje', async () => {
    boton('Nuevo usuario').click();
    await refrescar();
    llenarAlta();
    await enviar();
    expect(boton('Crear usuario').disabled).toBe(true);

    http
      .expectOne((r) => r.method === 'POST' && r.url === URL)
      .flush(
        {
          estado: 409,
          mensaje: 'Ya existe un usuario con el nombre: lramos',
          ruta: '/api/usuarios',
        },
        { status: 409, statusText: 'Conflict' },
      );
    await refrescar();

    expect(boton('Crear usuario').disabled).toBe(false);
    expect(raiz.querySelector('aside .alerta-error')?.textContent?.trim()).toBe(
      'Ya existe un usuario con el nombre: lramos',
    );
  });

  it('el cambio de contrasena exige una nueva distinta de la actual y confirmada', async () => {
    boton('Contraseña').click();
    await refrescar();

    escribir('passwordActual', 'rdiaz123');
    escribir('passwordNuevo', 'rdiaz123');
    escribir('confirmarPasswordNuevo', 'rdiaz124');
    await enviar();

    expect(errores()).toEqual([
      'La contraseña nueva debe ser distinta de la actual.',
      'Las contraseñas no coinciden.',
    ]);
    http.expectNone(`${URL}/3/password`);

    escribir('passwordNuevo', 'rdiaz456');
    escribir('confirmarPasswordNuevo', 'rdiaz456');
    await enviar();

    const req = http.expectOne(`${URL}/3/password`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ passwordActual: 'rdiaz123', passwordNuevo: 'rdiaz456' });
    req.flush(rdiaz);
    await refrescar();
    expect(raiz.querySelector('aside')).toBeNull();
  });
});
