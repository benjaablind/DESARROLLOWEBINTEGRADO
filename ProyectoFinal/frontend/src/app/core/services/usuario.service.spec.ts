import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import {
  ActualizarUsuarioRequest,
  CambiarPasswordRequest,
  Odontologo,
  PermisosResponse,
  Usuario,
  UsuarioRequest,
} from '../models/usuario.model';
import { UsuarioService } from './usuario.service';

const URL = `${environment.apiUrl}/usuarios`;

const admin: Usuario = {
  id: 1,
  nombres: 'Ana',
  apellidos: 'Torres',
  nombreCompleto: 'Ana Torres',
  email: 'admin@clinica.pe',
  usuario: 'admin',
  rol: 'ADMINISTRADOR',
  activo: true,
  fechaRegistro: '2026-03-01T08:00:00',
};

const nuevo: UsuarioRequest = {
  nombres: 'Rosa',
  apellidos: 'Diaz',
  email: 'rdiaz@clinica.pe',
  usuario: 'rdiaz',
  password: 'rdiaz123',
  rol: 'RECEPCIONISTA',
};

describe('UsuarioService', () => {
  let servicio: UsuarioService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(UsuarioService);
    http = TestBed.inject(HttpTestingController);
  });

  // Ninguna prueba debe dejar peticiones sin responder ni hacer peticiones de mas.
  afterEach(() => http.verify());

  it('listar() sin filtros hace GET /usuarios sin parametros', () => {
    let recibido: Usuario[] = [];
    servicio.listar().subscribe((lista) => (recibido = lista));

    const req = http.expectOne(URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).toEqual([]);
    req.flush([admin]);

    expect(recibido).toEqual([admin]);
  });

  it('listar() envia rol y activo como parametros, incluido activo=false', () => {
    servicio.listar('ODONTOLOGO', false).subscribe();

    const req = http.expectOne((r) => r.url === URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('rol')).toBe('ODONTOLOGO');
    expect(req.request.params.get('activo')).toBe('false');
    expect(req.request.urlWithParams).toBe(`${URL}?rol=ODONTOLOGO&activo=false`);
    req.flush([]);
  });

  it('listarOdontologos() hace GET /usuarios/odontologos', () => {
    const odontologos: Odontologo[] = [{ id: 2, nombreCompleto: 'Luis Ramos' }];
    let recibido: Odontologo[] = [];
    servicio.listarOdontologos().subscribe((lista) => (recibido = lista));

    const req = http.expectOne(`${URL}/odontologos`);
    expect(req.request.method).toBe('GET');
    req.flush(odontologos);

    expect(recibido).toEqual(odontologos);
  });

  it('buscarPorId() hace GET /usuarios/{id}', () => {
    let recibido: Usuario | undefined;
    servicio.buscarPorId(1).subscribe((u) => (recibido = u));

    const req = http.expectOne(`${URL}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(admin);

    expect(recibido?.usuario).toBe('admin');
  });

  it('crear() hace POST /usuarios con el UsuarioRequest como cuerpo', () => {
    let recibido: Usuario | undefined;
    servicio.crear(nuevo).subscribe((u) => (recibido = u));

    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(nuevo);
    req.flush({ ...admin, id: 3, usuario: 'rdiaz' }, { status: 201, statusText: 'Created' });

    expect(recibido?.id).toBe(3);
  });

  it('actualizar() hace PUT /usuarios/{id} sin credenciales en el cuerpo', () => {
    const cambios: ActualizarUsuarioRequest = {
      nombres: 'Ana Maria',
      apellidos: 'Torres',
      email: 'ana@clinica.pe',
      rol: 'ADMINISTRADOR',
    };
    servicio.actualizar(1, cambios).subscribe();

    const req = http.expectOne(`${URL}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(cambios);
    expect(req.request.body).not.toHaveProperty('password');
    req.flush({ ...admin, ...cambios });
  });

  it('cambiarPassword() hace PATCH /usuarios/{id}/password con las dos contrasenas', () => {
    const datos: CambiarPasswordRequest = { passwordActual: 'admin123', passwordNuevo: 'admin456' };
    servicio.cambiarPassword(1, datos).subscribe();

    const req = http.expectOne(`${URL}/1/password`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(datos);
    req.flush(admin);
  });

  it('activar() hace PATCH /usuarios/{id}/activar con cuerpo vacio', () => {
    servicio.activar(4).subscribe();

    const req = http.expectOne(`${URL}/4/activar`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({});
    req.flush({ ...admin, id: 4, activo: true });
  });

  it('desactivar() hace PATCH /usuarios/{id}/desactivar con cuerpo vacio', () => {
    let recibido: Usuario | undefined;
    servicio.desactivar(4).subscribe((u) => (recibido = u));

    const req = http.expectOne(`${URL}/4/desactivar`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({});
    req.flush({ ...admin, id: 4, activo: false });

    expect(recibido?.activo).toBe(false);
  });

  it('eliminar() hace DELETE /usuarios/{id} y acepta el 204 sin cuerpo', () => {
    let completo = false;
    servicio.eliminar(4).subscribe({ complete: () => (completo = true) });

    const req = http.expectOne(`${URL}/4`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(completo).toBe(true);
  });

  it('permisos() hace GET /usuarios/{id}/permisos', () => {
    const permisos: PermisosResponse = {
      rol: 'ASISTENTE',
      permisos: ['pacientes:leer', 'citas:leer'],
    };
    let recibido: PermisosResponse | undefined;
    servicio.permisos(5).subscribe((p) => (recibido = p));

    const req = http.expectOne(`${URL}/5/permisos`);
    expect(req.request.method).toBe('GET');
    req.flush(permisos);

    expect(recibido).toEqual(permisos);
  });

  describe('errores', () => {
    it('crear() entrega el mensaje del backend cuando responde 409', () => {
      let fallo: ErrorApi | undefined;
      servicio.crear(nuevo).subscribe({
        next: () => expect.unreachable('no debia crearse'),
        error: (e: ErrorApi) => (fallo = e),
      });

      http.expectOne(URL).flush(
        {
          estado: 409,
          mensaje: 'Ya existe un usuario con el nombre: rdiaz',
          ruta: '/api/usuarios',
          fechaHora: '2026-11-14T10:00:00',
        },
        { status: 409, statusText: 'Conflict' },
      );

      expect(fallo?.estado).toBe(409);
      expect(fallo?.mensaje).toBe('Ya existe un usuario con el nombre: rdiaz');
    });

    it('cambiarPassword() con 400 sin cuerpo JSON usa el mensaje por estado', () => {
      let fallo: ErrorApi | undefined;
      servicio
        .cambiarPassword(1, { passwordActual: 'mala', passwordNuevo: 'nueva123' })
        .subscribe({ error: (e: ErrorApi) => (fallo = e) });

      http.expectOne(`${URL}/1/password`).flush(null, { status: 400, statusText: 'Bad Request' });

      expect(fallo?.estado).toBe(400);
      expect(fallo?.mensaje).toBe('Los datos enviados no son validos');
    });

    it('listar() sin servidor (estado 0) avisa que no hay conexion', () => {
      let fallo: ErrorApi | undefined;
      servicio.listar().subscribe({ error: (e: ErrorApi) => (fallo = e) });

      http.expectOne(URL).error(new ProgressEvent('error'));

      expect(fallo?.estado).toBe(0);
      expect(fallo?.mensaje).toBe('No se pudo conectar con el servidor');
    });
  });
});
