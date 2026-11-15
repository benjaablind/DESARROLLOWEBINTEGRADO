import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import {
  Odontograma,
  OdontogramaHistorial,
  OdontogramaPieza,
  OdontogramaRegistroRequest,
  ResumenOdontograma,
} from '../models/odontograma.model';
import { OdontogramaService } from './odontograma.service';

/** Registro de una pieza tal como lo devuelve la API. */
function crearPieza(cambios: Partial<OdontogramaPieza> = {}): OdontogramaPieza {
  return {
    numeroPieza: 46,
    estado: 'OBTURADO',
    superficie: 'Oclusal',
    diagnostico: null,
    tratamiento: null,
    observaciones: null,
    fecha: '2026-09-04T10:00:00',
    odontologoId: 3,
    registroId: 12,
    ...cambios,
  };
}

describe('OdontogramaService', () => {
  let servicio: OdontogramaService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/odontograma`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(OdontogramaService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('obtenerActual hace GET /odontograma/paciente/{id} sin parametros', () => {
    const odontograma: Odontograma = {
      pacienteId: 2,
      pacienteNombre: 'Ana Torres',
      piezas: [crearPieza()],
      totalRegistros: 3,
    };
    let recibido: Odontograma | undefined;

    servicio.obtenerActual(2).subscribe((datos) => (recibido = datos));

    const req = http.expectOne(`${base}/paciente/2`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys().length).toBe(0);
    req.flush(odontograma);

    expect(recibido).toEqual(odontograma);
  });

  it('historialDePieza hace GET /odontograma/paciente/{id}/pieza/{numero}', () => {
    const historial: OdontogramaHistorial = {
      pacienteId: 2,
      numeroPieza: 46,
      registros: [crearPieza(), crearPieza({ estado: 'CARIES', registroId: 5 })],
    };
    let recibido: OdontogramaHistorial | undefined;

    servicio.historialDePieza(2, 46).subscribe((datos) => (recibido = datos));

    const req = http.expectOne(`${base}/paciente/2/pieza/46`);
    expect(req.request.method).toBe('GET');
    req.flush(historial);

    expect(recibido?.registros.length).toBe(2);
  });

  it('resumenEstados hace GET /odontograma/paciente/{id}/resumen', () => {
    const resumen: ResumenOdontograma = { CARIES: 3, OBTURADO: 5 };
    let recibido: ResumenOdontograma | undefined;

    servicio.resumenEstados(2).subscribe((datos) => (recibido = datos));

    const req = http.expectOne(`${base}/paciente/2/resumen`);
    expect(req.request.method).toBe('GET');
    req.flush(resumen);

    expect(recibido).toEqual(resumen);
  });

  it('registrar hace POST /odontograma con el cuerpo tal cual', () => {
    const datos: OdontogramaRegistroRequest = {
      pacienteId: 2,
      odontologoId: 3,
      numeroPieza: 46,
      estado: 'OBTURADO',
      superficie: 'Oclusal',
      diagnostico: null,
      tratamiento: null,
      observaciones: null,
    };
    let recibido: OdontogramaPieza | undefined;

    servicio.registrar(datos).subscribe((creado) => (recibido = creado));

    const req = http.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(datos);
    req.flush(crearPieza(), { status: 201, statusText: 'Created' });

    expect(recibido?.registroId).toBe(12);
  });

  it('registrar entrega el mensaje del backend para una pieza que no es FDI (400)', () => {
    let error: ErrorApi | undefined;

    servicio
      .registrar({ pacienteId: 2, odontologoId: 3, numeroPieza: 19, estado: 'CARIES' })
      .subscribe({
        next: () => {
          throw new Error('La pieza 19 no deberia registrarse');
        },
        error: (fallo: ErrorApi) => (error = fallo),
      });

    const req = http.expectOne(base);
    expect(req.request.body.numeroPieza).toBe(19);
    req.flush(
      {
        estado: 400,
        mensaje: 'El numero de pieza 19 no es valido en la notacion FDI',
        ruta: '/api/odontograma',
        fechaHora: '2026-09-04T10:00:00',
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(error?.estado).toBe(400);
    expect(error?.mensaje).toBe('El numero de pieza 19 no es valido en la notacion FDI');
  });

  it('historialDePieza traduce un 403 sin cuerpo a un mensaje de permisos', () => {
    let error: ErrorApi | undefined;

    servicio.historialDePieza(2, 46).subscribe({ error: (fallo: ErrorApi) => (error = fallo) });

    http
      .expectOne(`${base}/paciente/2/pieza/46`)
      .flush(null, { status: 403, statusText: 'Forbidden' });

    expect(error?.estado).toBe(403);
    expect(error?.mensaje).toBe('No tiene permisos para realizar esta accion');
  });
});
