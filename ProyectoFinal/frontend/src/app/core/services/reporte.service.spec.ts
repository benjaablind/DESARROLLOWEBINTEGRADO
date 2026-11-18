import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { Observable } from 'rxjs';

import { environment } from '../../../environments/environment';
import { ErrorApi } from '../models/comun.model';
import { ReporteService } from './reporte.service';

const URL = `${environment.apiUrl}/reportes`;

describe('ReporteService', () => {
  let servicio: ReporteService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    servicio = TestBed.inject(ReporteService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  const rango = { desde: '2026-01-01', hasta: '2026-03-31' };

  for (const [nombre, ruta] of [
    ['pacientes', 'pacientes'],
    ['citas', 'citas'],
    ['tratamientos', 'tratamientos'],
    ['ingresos', 'ingresos'],
  ] as const) {
    it(`${nombre}() hace GET /reportes/${ruta} con desde y hasta`, () => {
      const consulta: Observable<unknown> = servicio[nombre](rango);
      consulta.subscribe();

      const req = http.expectOne((r) => r.url === `${URL}/${ruta}`);
      expect(req.request.method).toBe('GET');
      expect(req.request.params.get('desde')).toBe('2026-01-01');
      expect(req.request.params.get('hasta')).toBe('2026-03-31');
      req.flush({});
    });
  }

  it('sin rango no envia parametros: el backend usa su rango por defecto', () => {
    servicio.ingresos().subscribe();

    const req = http.expectOne((r) => r.url === `${URL}/ingresos`);
    expect(req.request.params.keys()).toEqual([]);
    req.flush({});
  });

  it('pagosPendientes() hace GET /reportes/pagos-pendientes', () => {
    servicio.pagosPendientes().subscribe();
    const req = http.expectOne(`${URL}/pagos-pendientes`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('historialDePaciente() hace GET /reportes/historial/{id}', () => {
    servicio.historialDePaciente(12).subscribe();
    const req = http.expectOne(`${URL}/historial/12`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('entrega el mensaje del backend cuando el rango esta invertido', () => {
    let fallo: ErrorApi | undefined;
    servicio
      .citas({ desde: '2026-04-01', hasta: '2026-03-01' })
      .subscribe({ error: (e: ErrorApi) => (fallo = e) });

    http.expectOne((r) => r.url === `${URL}/citas`).flush(
      { estado: 400, mensaje: 'La fecha inicial no puede ser posterior a la fecha final' },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(fallo?.mensaje).toBe('La fecha inicial no puede ser posterior a la fecha final');
  });

  it('avisa cuando el servidor no responde (estado 0)', () => {
    let fallo: ErrorApi | undefined;
    servicio.pagosPendientes().subscribe({ error: (e: ErrorApi) => (fallo = e) });

    http.expectOne(`${URL}/pagos-pendientes`).error(new ProgressEvent('error'));

    expect(fallo?.estado).toBe(0);
    expect(fallo?.mensaje).toBe('No se pudo conectar con el servidor');
  });
});
