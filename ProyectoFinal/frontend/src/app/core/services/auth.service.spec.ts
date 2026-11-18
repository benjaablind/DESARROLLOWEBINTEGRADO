import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import { LoginResponse, Usuario } from '../models/usuario.model';
import { AuthService } from './auth.service';

const URL = `${environment.apiUrl}/auth`;
const CLAVE_ALMACEN = 'odontologia.sesion';

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

const respuestaLogin: LoginResponse = {
  tokenType: 'Bearer',
  accessToken: 'token.de.prueba',
  expiresInSeconds: 3600,
  usuario: admin,
};

describe('AuthService', () => {
  let servicio: AuthService;
  let http: HttpTestingController;

  /** AuthService lee localStorage al construirse: cada prueba arma su propio estado antes. */
  const crearServicio = () => {
    servicio = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  };

  /** Inicia sesion respondiendo el POST con el token de prueba. */
  const iniciarSesion = () => {
    servicio.login('admin', 'admin123').subscribe();
    http.expectOne(`${URL}/login`).flush(respuestaLogin);
  };

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
  });

  afterEach(() => {
    http.verify();
    localStorage.clear();
  });

  it('login() hace POST /auth/login con usuario y password, y guarda la sesion', () => {
    crearServicio();
    let recibido: LoginResponse | undefined;
    servicio.login('admin', 'admin123').subscribe((r) => (recibido = r));

    const req = http.expectOne(`${URL}/login`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ usuario: 'admin', password: 'admin123' });
    req.flush(respuestaLogin);

    expect(recibido).toEqual(respuestaLogin);
    expect(servicio.estaAutenticado()).toBe(true);
    expect(servicio.token()).toBe('token.de.prueba');
    expect(servicio.rol()).toBe('ADMINISTRADOR');
    expect(servicio.nombreUsuario()).toBe('Ana Torres');

    const guardada = JSON.parse(localStorage.getItem(CLAVE_ALMACEN) ?? '{}');
    expect(guardada.token).toBe('token.de.prueba');
    expect(guardada.expiraEn).toBeGreaterThan(Date.now());
  });

  it('login() con credenciales invalidas (401) entrega el mensaje y no guarda nada', () => {
    crearServicio();
    let fallo: ErrorApi | undefined;
    servicio.login('admin', 'mala').subscribe({
      next: () => expect.unreachable('no debia iniciar sesion'),
      error: (e: ErrorApi) => (fallo = e),
    });

    http.expectOne(`${URL}/login`).flush(
      {
        estado: 401,
        mensaje: 'Usuario o contrasena incorrectos',
        ruta: '/api/auth/login',
        fechaHora: '2026-11-14T10:00:00',
      },
      { status: 401, statusText: 'Unauthorized' },
    );

    expect(fallo?.estado).toBe(401);
    expect(fallo?.mensaje).toBe('Usuario o contrasena incorrectos');
    expect(servicio.estaAutenticado()).toBe(false);
    expect(servicio.token()).toBeNull();
    expect(localStorage.getItem(CLAVE_ALMACEN)).toBeNull();
  });

  it('perfil() hace GET /auth/perfil y actualiza el usuario de la sesion', () => {
    crearServicio();
    iniciarSesion();

    let recibido: Usuario | undefined;
    servicio.perfil().subscribe((u) => (recibido = u));

    const req = http.expectOne(`${URL}/perfil`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys()).toEqual([]);
    req.flush({ ...admin, nombreCompleto: 'Ana Maria Torres' });

    expect(recibido?.nombreCompleto).toBe('Ana Maria Torres');
    expect(servicio.nombreUsuario()).toBe('Ana Maria Torres');
    // El token no cambia: perfil() solo refresca los datos del usuario.
    expect(servicio.token()).toBe('token.de.prueba');
    const guardada = JSON.parse(localStorage.getItem(CLAVE_ALMACEN) ?? '{}');
    expect(guardada.usuario.nombreCompleto).toBe('Ana Maria Torres');
  });

  it('logout() con sesion hace POST /auth/logout y limpia la sesion local', () => {
    crearServicio();
    iniciarSesion();

    servicio.logout();

    const req = http.expectOne(`${URL}/logout`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({});
    // La sesion se descarta sin esperar la respuesta del servidor.
    expect(servicio.estaAutenticado()).toBe(false);
    expect(localStorage.getItem(CLAVE_ALMACEN)).toBeNull();
    req.flush({ mensaje: 'Sesion cerrada: el cliente debe descartar su token' });
  });

  it('logout() sin sesion no llama al backend', () => {
    crearServicio();

    servicio.logout();

    http.expectNone(`${URL}/logout`);
    expect(servicio.estaAutenticado()).toBe(false);
  });

  it('logout() ignora el fallo del servidor: la sesion igual queda cerrada', () => {
    crearServicio();
    iniciarSesion();

    servicio.logout();
    http.expectOne(`${URL}/logout`).error(new ProgressEvent('error'));

    expect(servicio.estaAutenticado()).toBe(false);
  });

  it('recupera una sesion vigente de localStorage y descarta una vencida', () => {
    localStorage.setItem(
      CLAVE_ALMACEN,
      JSON.stringify({ token: 'guardado', expiraEn: Date.now() - 1000, usuario: admin }),
    );
    crearServicio();
    expect(servicio.estaAutenticado()).toBe(false);

    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    localStorage.setItem(
      CLAVE_ALMACEN,
      JSON.stringify({ token: 'guardado', expiraEn: Date.now() + 60_000, usuario: admin }),
    );
    crearServicio();
    expect(servicio.token()).toBe('guardado');
    expect(servicio.usuarioActual()?.usuario).toBe('admin');
  });
});
