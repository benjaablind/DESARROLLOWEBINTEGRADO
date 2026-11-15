import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { CitaService } from './cita.service';
import { Cita, CitaRequest, ErrorApi } from '../models';

const URL_CITAS = `${environment.apiUrl}/citas`;

const CITA: Cita = {
  id: 7,
  pacienteId: 1,
  pacienteNombre: 'Ana Torres',
  odontologoId: 2,
  odontologoNombre: 'Luis Ramos',
  fechaHora: '2026-11-20T10:00:00',
  fechaHoraFin: '2026-11-20T10:30:00',
  duracionMinutos: 30,
  motivo: 'Control',
  estado: 'PENDIENTE',
  observaciones: null,
  citaOriginalId: null,
  fechaRegistro: '2026-11-14T09:00:00',
};

const PETICION: CitaRequest = {
  pacienteId: 1,
  odontologoId: 2,
  fechaHora: '2026-11-20T10:00:00',
  duracionMinutos: 30,
  motivo: 'Control',
  observaciones: null,
};

describe('CitaService', () => {
  let servicio: CitaService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(CitaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  describe('consultas', () => {
    it('listar sin filtros hace GET /citas sin parametros', () => {
      let respuesta: Cita[] | undefined;
      servicio.listar().subscribe((lista) => (respuesta = lista));

      const req = http.expectOne((r) => r.url === URL_CITAS);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.keys()).toEqual([]);
      req.flush([CITA]);

      expect(respuesta).toEqual([CITA]);
    });

    it('listar envia solo los filtros informados como parametros de consulta', () => {
      servicio
        .listar({ dia: '2026-11-20', pacienteId: 1, odontologoId: 2, estado: 'CONFIRMADA' })
        .subscribe();

      const req = http.expectOne((r) => r.url === URL_CITAS);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('dia')).toBe('2026-11-20');
      expect(req.request.params.get('pacienteId')).toBe('1');
      expect(req.request.params.get('odontologoId')).toBe('2');
      expect(req.request.params.get('estado')).toBe('CONFIRMADA');
      req.flush([]);
    });

    it('listar descarta los filtros vacios', () => {
      servicio.listar({ dia: '2026-11-20', pacienteId: undefined }).subscribe();

      const req = http.expectOne((r) => r.url === URL_CITAS);
      expect(req.request.params.keys()).toEqual(['dia']);
      req.flush([]);
    });

    it('listarPorPaciente reutiliza el listado con pacienteId y estado', () => {
      servicio.listarPorPaciente(1, 'ATENDIDA').subscribe();

      const req = http.expectOne((r) => r.url === URL_CITAS);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('pacienteId')).toBe('1');
      expect(req.request.params.get('estado')).toBe('ATENDIDA');
      req.flush([]);
    });

    it('agenda hace GET /citas/agenda?dia=', () => {
      let respuesta: Cita[] | undefined;
      servicio.agenda('2026-11-20').subscribe((lista) => (respuesta = lista));

      const req = http.expectOne((r) => r.url === `${URL_CITAS}/agenda`);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('dia')).toBe('2026-11-20');
      req.flush([CITA]);

      expect(respuesta).toEqual([CITA]);
    });

    it('disponibilidad hace GET /citas/disponibilidad con odontologoId y dia', () => {
      let horas: string[] | undefined;
      servicio.disponibilidad(2, '2026-11-20').subscribe((r) => (horas = r));

      const req = http.expectOne((r) => r.url === `${URL_CITAS}/disponibilidad`);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('odontologoId')).toBe('2');
      expect(req.request.params.get('dia')).toBe('2026-11-20');
      req.flush(['09:00', '10:00']);

      expect(horas).toEqual(['09:00', '10:00']);
    });

    it('buscarPorId hace GET /citas/{id}', () => {
      let respuesta: Cita | undefined;
      servicio.buscarPorId(7).subscribe((c) => (respuesta = c));

      const req = http.expectOne(`${URL_CITAS}/7`);
      expect(req.request.method).toBe('GET');
      req.flush(CITA);

      expect(respuesta).toEqual(CITA);
    });
  });

  describe('escrituras', () => {
    it('crear hace POST /citas con el CitaRequest como cuerpo', () => {
      let respuesta: Cita | undefined;
      servicio.crear(PETICION).subscribe((c) => (respuesta = c));

      const req = http.expectOne(URL_CITAS);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual(PETICION);
      req.flush(CITA, { status: 201, statusText: 'Created' });

      expect(respuesta).toEqual(CITA);
    });

    it('actualizar hace PUT /citas/{id} con el CitaRequest como cuerpo', () => {
      servicio.actualizar(7, PETICION).subscribe();

      const req = http.expectOne(`${URL_CITAS}/7`);
      expect(req.request.method).toBe('PUT');
      expect(req.request.body).toEqual(PETICION);
      req.flush(CITA);
    });

    it('cambiarEstado hace PATCH /citas/{id}/estado con el CitaEstadoRequest', () => {
      servicio
        .cambiarEstado(7, { estado: 'CONFIRMADA', observaciones: 'Por telefono' })
        .subscribe();

      const req = http.expectOne(`${URL_CITAS}/7/estado`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.body).toEqual({ estado: 'CONFIRMADA', observaciones: 'Por telefono' });
      req.flush({ ...CITA, estado: 'CONFIRMADA' });
    });

    it('confirmar hace PATCH /citas/{id}/confirmar', () => {
      servicio.confirmar(7).subscribe();

      const req = http.expectOne(`${URL_CITAS}/7/confirmar`);
      expect(req.request.method).toBe('PATCH');
      req.flush({ ...CITA, estado: 'CONFIRMADA' });
    });

    it('registrarAsistencia envia asistio como parametro de consulta (@RequestParam)', () => {
      servicio.registrarAsistencia(7, true).subscribe();

      const req = http.expectOne((r) => r.url === `${URL_CITAS}/7/asistencia`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.params.get('asistio')).toBe('true');
      expect(req.request.body).toEqual({});
      req.flush({ ...CITA, estado: 'ATENDIDA' });
    });

    it('registrarAsistencia tambien envia asistio=false', () => {
      servicio.registrarAsistencia(7, false).subscribe();

      const req = http.expectOne((r) => r.url === `${URL_CITAS}/7/asistencia`);
      expect(req.request.params.get('asistio')).toBe('false');
      req.flush({ ...CITA, estado: 'NO_ASISTIO' });
    });

    it('cancelar envia el motivo como parametro de consulta (@RequestParam)', () => {
      servicio.cancelar(7, 'Viaje').subscribe();

      const req = http.expectOne((r) => r.url === `${URL_CITAS}/7/cancelar`);
      expect(req.request.method).toBe('PATCH');
      expect(req.request.params.get('motivo')).toBe('Viaje');
      expect(req.request.body).toEqual({});
      req.flush({ ...CITA, estado: 'CANCELADA' });
    });

    it('cancelar sin motivo no envia el parametro', () => {
      servicio.cancelar(7).subscribe();

      const req = http.expectOne((r) => r.url === `${URL_CITAS}/7/cancelar`);
      expect(req.request.params.has('motivo')).toBe(false);
      req.flush({ ...CITA, estado: 'CANCELADA' });
    });

    it('reprogramar hace POST /citas/{id}/reprogramar con la nueva fecha', () => {
      let nueva: Cita | undefined;
      servicio
        .reprogramar(7, { nuevaFechaHora: '2026-11-21T11:00:00', motivo: 'Pedido del paciente' })
        .subscribe((c) => (nueva = c));

      const req = http.expectOne(`${URL_CITAS}/7/reprogramar`);
      expect(req.request.method).toBe('POST');
      expect(req.request.body).toEqual({
        nuevaFechaHora: '2026-11-21T11:00:00',
        motivo: 'Pedido del paciente',
      });
      const respuesta: Cita = { ...CITA, id: 8, citaOriginalId: 7 };
      req.flush(respuesta, { status: 201, statusText: 'Created' });

      expect(nueva).toEqual(respuesta);
    });

    it('eliminar hace DELETE /citas/{id}', () => {
      let completado = false;
      servicio.eliminar(7).subscribe({ complete: () => (completado = true) });

      const req = http.expectOne(`${URL_CITAS}/7`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null, { status: 204, statusText: 'No Content' });

      expect(completado).toBe(true);
    });
  });

  describe('errores', () => {
    it('un cruce de horario llega como ErrorApi con el mensaje del backend', () => {
      let error: ErrorApi | undefined;
      servicio.crear(PETICION).subscribe({ error: (e: ErrorApi) => (error = e) });

      http.expectOne(URL_CITAS).flush(
        {
          estado: 409,
          mensaje: 'El odontologo ya tiene una cita en ese horario',
          ruta: '/api/citas',
          fechaHora: '2026-11-14T10:00:00',
        },
        { status: 409, statusText: 'Conflict' },
      );

      expect(error?.estado).toBe(409);
      expect(error?.mensaje).toBe('El odontologo ya tiene una cita en ese horario');
    });

    it('una cita inexistente sin cuerpo de error usa el mensaje por estado', () => {
      let error: ErrorApi | undefined;
      servicio.buscarPorId(99).subscribe({ error: (e: ErrorApi) => (error = e) });

      http.expectOne(`${URL_CITAS}/99`).flush(null, { status: 404, statusText: 'Not Found' });

      expect(error?.estado).toBe(404);
      expect(error?.mensaje).toBe('El recurso solicitado no existe');
    });

    it('sin conexion avisa que el servidor no esta disponible', () => {
      let error: ErrorApi | undefined;
      servicio.agenda('2026-11-20').subscribe({ error: (e: ErrorApi) => (error = e) });

      http
        .expectOne((r) => r.url === `${URL_CITAS}/agenda`)
        .error(new ProgressEvent('error'), { status: 0, statusText: 'Unknown Error' });

      expect(error?.estado).toBe(0);
      expect(error?.mensaje).toBe('No se pudo conectar con el servidor');
    });
  });
});
