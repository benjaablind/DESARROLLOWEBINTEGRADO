import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import { AntecedentesRequest, Paciente, PacienteRequest } from '../models/paciente.model';
import { PacienteService } from './paciente.service';

const URL_PACIENTES = `${environment.apiUrl}/pacientes`;

const PACIENTE: Paciente = {
  id: 1,
  dni: '45678912',
  nombres: 'Ana Lucia',
  apellidos: 'Torres Vega',
  nombreCompleto: 'Ana Lucia Torres Vega',
  edad: 34,
  fechaNacimiento: '1992-04-18',
  sexo: 'F',
  telefono: '987654321',
  email: 'ana.torres@correo.pe',
  direccion: null,
  distrito: null,
  ciudad: 'Lima',
  estado: 'ACTIVO',
  antecedentes: {
    enfermedades: [],
    alergias: ['Penicilina'],
    medicamentos: [],
    habitos: [],
    antecedentesOdontologicos: null,
    observaciones: null,
  },
  fechaRegistro: '2026-09-04T10:00:00',
};

const SOLICITUD: PacienteRequest = {
  dni: '45678912',
  nombres: 'Ana Lucia',
  apellidos: 'Torres Vega',
  fechaNacimiento: '1992-04-18',
  sexo: 'F',
  telefono: '987654321',
  email: 'ana.torres@correo.pe',
  direccion: null,
  distrito: null,
  ciudad: 'Lima',
  antecedentes: { alergias: ['Penicilina'] },
};

describe('PacienteService', () => {
  let servicio: PacienteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(PacienteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Ninguna prueba debe dejar peticiones sin responder.
    http.verify();
  });

  describe('listar', () => {
    it('envia GET /pacientes con texto y estado como parametros', () => {
      let filas: unknown;
      servicio.listar('torres', 'ACTIVO').subscribe((r) => (filas = r));

      const req = http.expectOne((r) => r.url === URL_PACIENTES);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('texto')).toBe('torres');
      expect(req.request.params.get('estado')).toBe('ACTIVO');

      const respuesta = [
        {
          id: 1,
          dni: '45678912',
          nombreCompleto: 'Ana',
          edad: 34,
          telefono: null,
          estado: 'ACTIVO',
        },
      ];
      req.flush(respuesta);
      expect(filas).toEqual(respuesta);
    });

    it('omite los filtros que no se indicaron', () => {
      servicio.listar().subscribe();

      const req = http.expectOne((r) => r.url === URL_PACIENTES);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.keys()).toEqual([]);
      req.flush([]);
    });
  });

  it('buscarPorId envia GET /pacientes/{id}', () => {
    let ficha: Paciente | undefined;
    servicio.buscarPorId(1).subscribe((r) => (ficha = r));

    const req = http.expectOne(`${URL_PACIENTES}/1`);
    expect(req.request.method).toBe('GET');
    req.flush(PACIENTE);

    expect(ficha).toEqual(PACIENTE);
  });

  it('buscarPorDni envia GET /pacientes/dni/{dni}', () => {
    servicio.buscarPorDni('45678912').subscribe();

    const req = http.expectOne(`${URL_PACIENTES}/dni/45678912`);
    expect(req.request.method).toBe('GET');
    req.flush(PACIENTE);
  });

  it('crear envia POST /pacientes con el cuerpo tal cual', () => {
    let creado: Paciente | undefined;
    servicio.crear(SOLICITUD).subscribe((r) => (creado = r));

    const req = http.expectOne(URL_PACIENTES);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(SOLICITUD);
    req.flush(PACIENTE, { status: 201, statusText: 'Created' });

    expect(creado).toEqual(PACIENTE);
  });

  it('actualizar envia PUT /pacientes/{id} con el cuerpo', () => {
    servicio.actualizar(1, SOLICITUD).subscribe();

    const req = http.expectOne(`${URL_PACIENTES}/1`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(SOLICITUD);
    req.flush(PACIENTE);
  });

  it('actualizarAntecedentes envia PATCH /pacientes/{id}/antecedentes con las listas', () => {
    const datos: AntecedentesRequest = {
      enfermedades: ['Hipertensión, controlada'],
      alergias: ['Penicilina', 'Látex'],
      medicamentos: [],
      habitos: ['Bruxismo'],
      antecedentesOdontologicos: null,
      observaciones: 'Control cada 6 meses',
    };
    servicio.actualizarAntecedentes(1, datos).subscribe();

    const req = http.expectOne(`${URL_PACIENTES}/1/antecedentes`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual(datos);
    req.flush(PACIENTE);
  });

  it('cambiarEstado envia PATCH /pacientes/{id}/estado con { estado }', () => {
    servicio.cambiarEstado(1, 'INACTIVO').subscribe();

    const req = http.expectOne(`${URL_PACIENTES}/1/estado`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ estado: 'INACTIVO' });
    req.flush({ ...PACIENTE, estado: 'INACTIVO' });
  });

  it('eliminar envia DELETE /pacientes/{id}', () => {
    let completado = false;
    servicio.eliminar(1).subscribe({ complete: () => (completado = true) });

    const req = http.expectOne(`${URL_PACIENTES}/1`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });

    expect(completado).toBe(true);
  });

  describe('errores', () => {
    it('entrega el mensaje del backend cuando el paciente no existe (404)', () => {
      let error: ErrorApi | undefined;
      servicio.buscarPorId(99).subscribe({ error: (e: ErrorApi) => (error = e) });

      http.expectOne(`${URL_PACIENTES}/99`).flush(
        {
          estado: 404,
          mensaje: 'No existe paciente con id: 99',
          ruta: '/api/pacientes/99',
          fechaHora: '2026-09-04T16:44:26',
        },
        { status: 404, statusText: 'Not Found' },
      );

      expect(error?.estado).toBe(404);
      expect(error?.mensaje).toBe('No existe paciente con id: 99');
    });

    it('entrega el conflicto al eliminar un paciente con historial (409)', () => {
      let error: ErrorApi | undefined;
      servicio.eliminar(2).subscribe({ error: (e: ErrorApi) => (error = e) });

      const req = http.expectOne(`${URL_PACIENTES}/2`);
      expect(req.request.method).toBe('DELETE');
      req.flush(
        { estado: 409, mensaje: 'El paciente tiene registros asociados', ruta: '/api/pacientes/2' },
        { status: 409, statusText: 'Conflict' },
      );

      expect(error?.estado).toBe(409);
      expect(error?.mensaje).toBe('El paciente tiene registros asociados');
    });

    it('avisa que no hay conexion cuando el servidor no responde', () => {
      let error: ErrorApi | undefined;
      servicio.crear(SOLICITUD).subscribe({ error: (e: ErrorApi) => (error = e) });

      http.expectOne(URL_PACIENTES).error(new ProgressEvent('error'), { status: 0 });

      expect(error?.estado).toBe(0);
      expect(error?.mensaje).toBe('No se pudo conectar con el servidor');
    });
  });
});
