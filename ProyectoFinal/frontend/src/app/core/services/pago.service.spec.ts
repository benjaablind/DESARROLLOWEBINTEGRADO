import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import { EstadoCuenta, Pago, PagoRequest } from '../models/pago.model';
import { PagoService } from './pago.service';

const BASE = `${environment.apiUrl}/pagos`;

const PAGO: Pago = {
  id: 11,
  tratamientoId: 7,
  tratamientoNombre: 'Ortodoncia correctiva',
  pacienteId: 3,
  pacienteNombre: 'Ana Torres',
  monto: 250,
  metodo: 'YAPE',
  fecha: '2026-11-14T09:30:00',
  comprobante: 'OP-123456',
  observaciones: null,
};

describe('PagoService', () => {
  let servicio: PagoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(PagoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
  });

  it('listar sin filtros hace GET /pagos sin parametros', () => {
    let recibido: Pago[] | undefined;
    servicio.listar().subscribe((lista) => (recibido = lista));

    const peticion = http.expectOne(BASE);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.keys()).toEqual([]);
    peticion.flush([PAGO]);

    expect(recibido).toEqual([PAGO]);
  });

  it('listar envia los filtros informados como query params', () => {
    servicio.listar({ pacienteId: 3, tratamientoId: 7 }).subscribe();

    const peticion = http.expectOne((r) => r.url === BASE);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.get('pacienteId')).toBe('3');
    expect(peticion.request.params.get('tratamientoId')).toBe('7');
    expect(peticion.request.params.has('metodo')).toBe(false);
    peticion.flush([]);
  });

  it('listarPorPaciente usa el parametro pacienteId, no una ruta propia', () => {
    servicio.listarPorPaciente(3).subscribe();

    const peticion = http.expectOne((r) => r.url === BASE);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.get('pacienteId')).toBe('3');
    peticion.flush([]);
  });

  it('buscarPorId hace GET /pagos/{id}', () => {
    servicio.buscarPorId(11).subscribe();

    const peticion = http.expectOne(`${BASE}/11`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush(PAGO);
  });

  it('listarPorTratamiento hace GET /pagos/tratamiento/{tratamientoId}', () => {
    servicio.listarPorTratamiento(7).subscribe();

    const peticion = http.expectOne(`${BASE}/tratamiento/7`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush([PAGO]);
  });

  it('estadoCuenta hace GET /pagos/estado-cuenta/{pacienteId}', () => {
    const cuenta: EstadoCuenta = {
      pacienteId: 3,
      pacienteNombre: 'Ana Torres',
      totalTratamientos: 1500,
      totalPagado: 250,
      saldoPendiente: 1250,
      tratamientos: [],
    };
    let recibido: EstadoCuenta | undefined;
    servicio.estadoCuenta(3).subscribe((c) => (recibido = c));

    const peticion = http.expectOne(`${BASE}/estado-cuenta/3`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush(cuenta);

    expect(recibido?.saldoPendiente).toBe(1250);
  });

  it('registrar hace POST /pagos con el cuerpo del formulario', () => {
    const datos: PagoRequest = {
      tratamientoId: 7,
      monto: 250,
      metodo: 'YAPE',
      comprobante: 'OP-123456',
      observaciones: null,
    };
    let recibido: Pago | undefined;
    servicio.registrar(datos).subscribe((p) => (recibido = p));

    const peticion = http.expectOne(BASE);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(datos);
    peticion.flush(PAGO, { status: 201, statusText: 'Created' });

    expect(recibido?.id).toBe(11);
  });

  it('anular hace DELETE /pagos/{id}', () => {
    let completado = false;
    servicio.anular(11).subscribe({ complete: () => (completado = true) });

    const peticion = http.expectOne(`${BASE}/11`);
    expect(peticion.request.method).toBe('DELETE');
    peticion.flush(null, { status: 204, statusText: 'No Content' });

    expect(completado).toBe(true);
  });

  it('entrega el mensaje del backend cuando el monto excede el saldo (400)', () => {
    let error: ErrorApi | undefined;
    servicio
      .registrar({ tratamientoId: 7, monto: 2000, metodo: 'EFECTIVO' })
      .subscribe({ error: (e: ErrorApi) => (error = e) });

    http.expectOne(BASE).flush(
      {
        estado: 400,
        mensaje: 'El monto excede el saldo pendiente del tratamiento: saldo actual S/ 1250.00',
        ruta: '/api/pagos',
        fechaHora: '2026-11-14T10:00:00',
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(error?.estado).toBe(400);
    expect(error?.mensaje).toContain('saldo actual S/ 1250.00');
  });

  it('avisa que el servidor no esta disponible cuando no hay respuesta (estado 0)', () => {
    let error: ErrorApi | undefined;
    servicio.estadoCuenta(3).subscribe({ error: (e: ErrorApi) => (error = e) });

    http
      .expectOne(`${BASE}/estado-cuenta/3`)
      .error(new ProgressEvent('error'), { status: 0, statusText: 'Unknown Error' });

    expect(error?.estado).toBe(0);
    expect(error?.mensaje).toBe('No se pudo conectar con el servidor');
  });
});
