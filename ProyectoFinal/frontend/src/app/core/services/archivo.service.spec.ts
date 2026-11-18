import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import { ArchivoService } from './archivo.service';

const URL = `${environment.apiUrl}/archivos`;

describe('ArchivoService', () => {
  let servicio: ArchivoService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(ArchivoService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('listar() hace GET /archivos y solo envia los filtros con valor', () => {
    servicio.listar({ tipo: 'RADIOGRAFIA' }).subscribe();

    const req = http.expectOne((r) => r.url === URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('tipo')).toBe('RADIOGRAFIA');
    expect(req.request.params.has('pacienteId')).toBe(false);
    req.flush([]);
  });

  it('listarPorPaciente() hace GET /archivos/paciente/{id} con el tipo opcional', () => {
    servicio.listarPorPaciente(7, 'INFORME').subscribe();

    const req = http.expectOne((r) => r.url === `${URL}/paciente/7`);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('tipo')).toBe('INFORME');
    req.flush([]);
  });

  it('buscarPorId() hace GET /archivos/{id}', () => {
    servicio.buscarPorId(3).subscribe();
    const req = http.expectOne(`${URL}/3`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('construirFormData() arma los campos que espera el backend y omite los vacios', () => {
    const archivo = new File(['rx'], 'panoramica.png', { type: 'image/png' });

    const completo = servicio.construirFormData({
      archivo,
      pacienteId: 5,
      tipo: 'RADIOGRAFIA',
      descripcion: 'Control',
      historiaClinicaId: 9,
    });
    expect((completo.get('archivo') as File).name).toBe('panoramica.png');
    expect(completo.get('pacienteId')).toBe('5');
    expect(completo.get('tipo')).toBe('RADIOGRAFIA');
    expect(completo.get('descripcion')).toBe('Control');
    expect(completo.get('historiaClinicaId')).toBe('9');

    const minimo = servicio.construirFormData({ archivo, pacienteId: 5, tipo: 'FOTOGRAFIA' });
    expect(minimo.has('descripcion')).toBe(false);
    expect(minimo.has('historiaClinicaId')).toBe(false);
    // Quien sube el archivo sale del token: nunca se envia usuarioId.
    expect(minimo.has('usuarioId')).toBe(false);
  });

  it('subir() hace POST /archivos con el FormData sin fijar Content-Type', () => {
    const datos = new FormData();
    datos.append('pacienteId', '5');
    servicio.subir(datos).subscribe();

    const req = http.expectOne(URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBe(datos);
    // El navegador agrega multipart/form-data con su boundary.
    expect(req.request.headers.has('Content-Type')).toBe(false);
    req.flush({ id: 1 }, { status: 201, statusText: 'Created' });
  });

  it('actualizar() hace PUT /archivos/{id} solo con los metadatos', () => {
    servicio.actualizar(4, { tipo: 'RECETA', descripcion: 'Amoxicilina' }).subscribe();

    const req = http.expectOne(`${URL}/4`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ tipo: 'RECETA', descripcion: 'Amoxicilina' });
    req.flush({});
  });

  it('eliminar() hace DELETE /archivos/{id}', () => {
    servicio.eliminar(4).subscribe();
    const req = http.expectOne(`${URL}/4`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('descargar() pide el binario como Blob y urlDescarga() arma la URL absoluta', () => {
    let recibido: Blob | undefined;
    servicio.descargar(2).subscribe((b) => (recibido = b));

    const req = http.expectOne(`${URL}/2/descargar`);
    expect(req.request.responseType).toBe('blob');
    req.flush(new Blob(['pdf']));

    expect(recibido).toBeInstanceOf(Blob);
    expect(servicio.urlDescarga(2)).toBe(`${URL}/2/descargar`);
  });

  it('entrega el mensaje del backend cuando rechaza la extension', () => {
    let fallo: ErrorApi | undefined;
    servicio.subir(new FormData()).subscribe({ error: (e: ErrorApi) => (fallo = e) });

    http.expectOne(URL).flush(
      { estado: 400, mensaje: 'La extension .exe no esta permitida', ruta: '/api/archivos' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(fallo?.estado).toBe(400);
    expect(fallo?.mensaje).toBe('La extension .exe no esta permitida');
  });

  it('traduce un 403 sin cuerpo a un mensaje de permisos', () => {
    let fallo: ErrorApi | undefined;
    servicio.listar().subscribe({ error: (e: ErrorApi) => (fallo = e) });

    http.expectOne(URL).flush(null, { status: 403, statusText: 'Forbidden' });

    expect(fallo?.mensaje).toBe('No tiene permisos para realizar esta accion');
  });
});
