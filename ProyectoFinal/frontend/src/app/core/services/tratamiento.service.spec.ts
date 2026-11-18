import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import { SesionRequest, Tratamiento, TratamientoRequest } from '../models/tratamiento.model';
import { TratamientoService } from './tratamiento.service';

const BASE = `${environment.apiUrl}/tratamientos`;

const TRATAMIENTO: Tratamiento = {
  id: 7,
  pacienteId: 3,
  pacienteNombre: 'Ana Torres',
  odontologoId: 2,
  odontologoNombre: 'Luis Ramos',
  nombre: 'Ortodoncia correctiva',
  descripcion: null,
  precio: 1500,
  estado: 'PENDIENTE',
  fechaInicio: null,
  fechaFin: null,
  observaciones: null,
  sesiones: [],
  totalPagado: 0,
  saldoPendiente: 1500,
  fechaRegistro: '2026-11-01T10:00:00',
};

describe('TratamientoService', () => {
  let servicio: TratamientoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(TratamientoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Ninguna peticion debe quedar sin atender ni hacerse de mas.
    http.verify();
  });

  it('listar sin filtros hace GET /tratamientos sin parametros', () => {
    let recibido: Tratamiento[] | undefined;
    servicio.listar().subscribe((lista) => (recibido = lista));

    const peticion = http.expectOne(BASE);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.keys()).toEqual([]);
    peticion.flush([TRATAMIENTO]);

    expect(recibido).toEqual([TRATAMIENTO]);
  });

  it('listar envia solo los filtros informados como query params', () => {
    servicio.listar({ pacienteId: 3, estado: 'EN_PROCESO' }).subscribe();

    const peticion = http.expectOne((r) => r.url === BASE);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.get('pacienteId')).toBe('3');
    expect(peticion.request.params.get('estado')).toBe('EN_PROCESO');
    expect(peticion.request.params.has('odontologoId')).toBe(false);
    peticion.flush([]);
  });

  it('listarPorPaciente filtra por el parametro pacienteId', () => {
    servicio.listarPorPaciente(3).subscribe();

    const peticion = http.expectOne((r) => r.url === BASE);
    expect(peticion.request.method).toBe('GET');
    expect(peticion.request.params.get('pacienteId')).toBe('3');
    peticion.flush([]);
  });

  it('buscarPorId hace GET /tratamientos/{id}', () => {
    let recibido: Tratamiento | undefined;
    servicio.buscarPorId(7).subscribe((t) => (recibido = t));

    const peticion = http.expectOne(`${BASE}/7`);
    expect(peticion.request.method).toBe('GET');
    peticion.flush(TRATAMIENTO);

    expect(recibido?.id).toBe(7);
  });

  it('crear hace POST /tratamientos con el cuerpo del formulario', () => {
    const datos: TratamientoRequest = {
      pacienteId: 3,
      odontologoId: 2,
      nombre: 'Ortodoncia correctiva',
      descripcion: null,
      precio: 1500,
      fechaInicio: '2026-11-20',
      observaciones: null,
    };
    servicio.crear(datos).subscribe();

    const peticion = http.expectOne(BASE);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(datos);
    peticion.flush(TRATAMIENTO, { status: 201, statusText: 'Created' });
  });

  it('actualizar hace PUT /tratamientos/{id} con el cuerpo completo', () => {
    const datos: TratamientoRequest = {
      pacienteId: 3,
      odontologoId: 2,
      nombre: 'Ortodoncia',
      precio: 1800.5,
    };
    servicio.actualizar(7, datos).subscribe();

    const peticion = http.expectOne(`${BASE}/7`);
    expect(peticion.request.method).toBe('PUT');
    expect(peticion.request.body).toEqual(datos);
    peticion.flush({ ...TRATAMIENTO, precio: 1800.5 });
  });

  it('cambiarEstado hace PATCH /tratamientos/{id}/estado con { estado }', () => {
    servicio.cambiarEstado(7, 'APROBADO').subscribe();

    const peticion = http.expectOne(`${BASE}/7/estado`);
    expect(peticion.request.method).toBe('PATCH');
    expect(peticion.request.body).toEqual({ estado: 'APROBADO' });
    peticion.flush({ ...TRATAMIENTO, estado: 'APROBADO' });
  });

  it('eliminar hace DELETE /tratamientos/{id}', () => {
    let completado = false;
    servicio.eliminar(7).subscribe({ complete: () => (completado = true) });

    const peticion = http.expectOne(`${BASE}/7`);
    expect(peticion.request.method).toBe('DELETE');
    peticion.flush(null, { status: 204, statusText: 'No Content' });

    expect(completado).toBe(true);
  });

  it('agregarSesion hace POST /tratamientos/{id}/sesiones con la sesion', () => {
    const sesion: SesionRequest = {
      fecha: '2026-11-21',
      descripcion: 'Colocacion de brackets',
      observaciones: null,
    };
    servicio.agregarSesion(7, sesion).subscribe();

    const peticion = http.expectOne(`${BASE}/7/sesiones`);
    expect(peticion.request.method).toBe('POST');
    expect(peticion.request.body).toEqual(sesion);
    peticion.flush(TRATAMIENTO, { status: 201, statusText: 'Created' });
  });

  it('cambiarEstadoSesion hace PATCH /tratamientos/{id}/sesiones/{sesionId}', () => {
    servicio
      .cambiarEstadoSesion(7, 2, { realizada: true, observaciones: 'Sin molestias' })
      .subscribe();

    const peticion = http.expectOne(`${BASE}/7/sesiones/2`);
    expect(peticion.request.method).toBe('PATCH');
    expect(peticion.request.body).toEqual({ realizada: true, observaciones: 'Sin molestias' });
    peticion.flush(TRATAMIENTO);
  });

  it('eliminarSesion hace DELETE /tratamientos/{id}/sesiones/{sesionId}', () => {
    servicio.eliminarSesion(7, 2).subscribe();

    const peticion = http.expectOne(`${BASE}/7/sesiones/2`);
    expect(peticion.request.method).toBe('DELETE');
    peticion.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('traduce el error del backend a ErrorApi con su mensaje', () => {
    let error: ErrorApi | undefined;
    servicio.cambiarEstado(7, 'APROBADO').subscribe({ error: (e: ErrorApi) => (error = e) });

    http.expectOne(`${BASE}/7/estado`).flush(
      {
        estado: 400,
        mensaje:
          'No se puede cambiar el estado de un tratamiento COMPLETADO: ya es un estado final',
        ruta: '/api/tratamientos/7/estado',
        fechaHora: '2026-11-14T10:00:00',
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(error?.estado).toBe(400);
    expect(error?.mensaje).toContain('ya es un estado final');
  });

  it('usa el mensaje por estado cuando el backend no envia JSON (404)', () => {
    let error: ErrorApi | undefined;
    servicio.buscarPorId(99).subscribe({ error: (e: ErrorApi) => (error = e) });

    http.expectOne(`${BASE}/99`).flush(null, { status: 404, statusText: 'Not Found' });

    expect(error?.estado).toBe(404);
    expect(error?.mensaje).toBe('El recurso solicitado no existe');
  });
});
