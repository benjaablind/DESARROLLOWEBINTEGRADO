import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import { HistoriaClinica, HistoriaClinicaRequest } from '../models/historia.model';
import { HistoriaClinicaService } from './historia-clinica.service';

/** Consulta de ejemplo tal como la devuelve la API. */
function crearHistoria(cambios: Partial<HistoriaClinica> = {}): HistoriaClinica {
  return {
    id: 7,
    pacienteId: 2,
    pacienteNombre: 'Ana Torres',
    odontologoId: 3,
    odontologoNombre: 'Luis Ramos',
    citaId: null,
    fecha: '2026-09-04T10:00:00',
    motivoConsulta: 'Dolor en la pieza 46',
    anamnesis: null,
    examenClinico: null,
    diagnostico: 'Caries profunda',
    procedimiento: null,
    tratamiento: null,
    medicamentos: null,
    observaciones: null,
    anulada: false,
    motivoAnulacion: null,
    ...cambios,
  };
}

describe('HistoriaClinicaService', () => {
  let servicio: HistoriaClinicaService;
  let http: HttpTestingController;
  const base = `${environment.apiUrl}/historias`;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(HistoriaClinicaService);
    http = TestBed.inject(HttpTestingController);
  });

  // Ninguna prueba puede dejar peticiones sin responder ni hacer peticiones de mas.
  afterEach(() => http.verify());

  it('listarPorPaciente hace GET /historias/paciente/{id} sin parametros', () => {
    const lista = [crearHistoria(), crearHistoria({ id: 8, anulada: true })];
    let recibida: HistoriaClinica[] | undefined;

    servicio.listarPorPaciente(2).subscribe((datos) => (recibida = datos));

    const req = http.expectOne(`${base}/paciente/2`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.keys().length).toBe(0);
    req.flush(lista);

    expect(recibida).toEqual(lista);
  });

  it('buscarPorId hace GET /historias/{id}', () => {
    let recibida: HistoriaClinica | undefined;

    servicio.buscarPorId(7).subscribe((datos) => (recibida = datos));

    const req = http.expectOne(`${base}/7`);
    expect(req.request.method).toBe('GET');
    req.flush(crearHistoria());

    expect(recibida?.id).toBe(7);
  });

  it('crear hace POST /historias con el cuerpo tal cual', () => {
    const datos: HistoriaClinicaRequest = {
      pacienteId: 2,
      odontologoId: 3,
      citaId: 15,
      motivoConsulta: 'Dolor en la pieza 46',
      diagnostico: 'Caries profunda',
      anamnesis: null,
      examenClinico: null,
      procedimiento: 'Obturacion',
      tratamiento: null,
      medicamentos: null,
      observaciones: null,
    };
    let recibida: HistoriaClinica | undefined;

    servicio.crear(datos).subscribe((creada) => (recibida = creada));

    const req = http.expectOne(base);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(datos);
    req.flush(crearHistoria({ citaId: 15 }), { status: 201, statusText: 'Created' });

    expect(recibida?.citaId).toBe(15);
  });

  it('anular hace PATCH /historias/{id}/anular con { motivo }', () => {
    let recibida: HistoriaClinica | undefined;

    servicio.anular(7, 'Registrada en el paciente equivocado').subscribe((h) => (recibida = h));

    const req = http.expectOne(`${base}/7/anular`);
    expect(req.request.method).toBe('PATCH');
    expect(req.request.body).toEqual({ motivo: 'Registrada en el paciente equivocado' });
    req.flush(
      crearHistoria({ anulada: true, motivoAnulacion: 'Registrada en el paciente equivocado' }),
    );

    expect(recibida?.anulada).toBe(true);
  });

  it('anular entrega el mensaje del backend cuando la consulta ya estaba anulada (400)', () => {
    let error: ErrorApi | undefined;

    servicio.anular(7, 'Duplicada').subscribe({
      next: () => {
        throw new Error('No deberia anular dos veces');
      },
      error: (fallo: ErrorApi) => (error = fallo),
    });

    http.expectOne(`${base}/7/anular`).flush(
      {
        estado: 400,
        mensaje: 'La consulta ya fue anulada',
        ruta: '/api/historias/7/anular',
        fechaHora: '2026-09-04T10:00:00',
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(error?.estado).toBe(400);
    expect(error?.mensaje).toBe('La consulta ya fue anulada');
  });

  it('listarPorPaciente traduce un paciente inexistente (404 sin cuerpo) a ErrorApi', () => {
    let error: ErrorApi | undefined;

    servicio.listarPorPaciente(999).subscribe({ error: (fallo: ErrorApi) => (error = fallo) });

    http.expectOne(`${base}/paciente/999`).flush(null, { status: 404, statusText: 'Not Found' });

    expect(error?.estado).toBe(404);
    expect(error?.mensaje).toBe('El recurso solicitado no existe');
  });

  it('crear avisa cuando el servidor no responde (estado 0)', () => {
    let error: ErrorApi | undefined;

    servicio
      .crear({ pacienteId: 2, odontologoId: 3, motivoConsulta: 'Control', diagnostico: 'Sano' })
      .subscribe({ error: (fallo: ErrorApi) => (error = fallo) });

    http.expectOne(base).error(new ProgressEvent('error'));

    expect(error?.estado).toBe(0);
    expect(error?.mensaje).toBe('No se pudo conectar con el servidor');
  });
});
